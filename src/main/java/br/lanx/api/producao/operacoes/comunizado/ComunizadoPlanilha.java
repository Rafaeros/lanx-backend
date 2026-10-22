package br.lanx.api.producao.operacoes.comunizado;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import br.lanx.api.producao.operacoes.comunizado.domain.CabecalhoComunizado;
import br.lanx.api.producao.operacoes.comunizado.domain.CodigoCabo;
import br.lanx.api.producao.operacoes.comunizado.domain.CorCabo;
import br.lanx.api.producao.operacoes.comunizado.domain.CoresCabos;
import br.lanx.api.producao.operacoes.comunizado.domain.ItemCorte;

/**
 * Comunizado no layout do modelo "ORN105 ... .xlsx" (docs/): bloco de cabeçalho
 * (realizado em, doc. referência, produto, início, quantidade, fim, OP) e a
 * tabela PROD. | OPERADOR | CIRC | [GRAVAÇÃO] | COMP | TIPO | TERMINAL A | DESP. A |
 * TERMINAL B | DESP. B. GRAVAÇÃO só aparece quando a IT a tem.
 * <p>
 * Cores (cores-cabos.csv): o texto da linha é a cor do cabo; em cabos bicolores
 * a célula TIPO tem gradiente cor → faixa.
 * <p>
 * Impressão: A4 paisagem com {@value #ITENS_POR_PAGINA} itens por página (mesmo
 * limite da IT) e o cabeçalho do comunizado repetido em cada página.
 */
public class ComunizadoPlanilha {

    private static final String ABA = "Comunizado";
    private static final String FONTE = "Calibri";
    private static final String LOGO = "/producao/comunizado/logo.png";

    /** Itens que cabem numa página A4 com o cabeçalho (o mesmo limite das abas da IT). */
    static final int ITENS_POR_PAGINA = 22;

    private static final int LINHA_CABECALHO_TABELA = 9; // linha 10 no Excel

    /** Quanto as cores do gradiente são clareadas para o texto continuar legível. */
    private static final double CLAREAR_GRADIENTE = 0.35;

    /** Uma coluna da tabela: título, largura (caracteres, como no modelo) e valor de cada linha. */
    private record Coluna(String titulo, double largura, Function<Linha, Object> valor) {
    }

    /** Uma linha da tabela já resolvida: item, cabo decodificado e defaults do cabeçalho. */
    private record Linha(ItemCorte item, Optional<CodigoCabo> cabo, String produto, String operador) {
    }

    private static final Coluna TIPO = new Coluna("TIPO", 22.1, l -> l.item().tipo());

    /** Colunas do ORN105; GRAVAÇÃO entra depois de CIRC quando a IT tem. */
    private static List<Coluna> colunas(boolean comGravacao) {
        List<Coluna> colunas = new ArrayList<>(List.of(
                new Coluna("PROD.", 20.9, Linha::produto),
                new Coluna("OPERADOR", 26.3, Linha::operador),
                new Coluna("CIRC", 13.9, l -> l.item().circuito())));
        if (comGravacao) {
            colunas.add(new Coluna("GRAVAÇÃO", 30.0, l -> l.item().gravacao()));
        }
        colunas.addAll(List.of(
                new Coluna("COMP", 13.0, l -> l.item().comprimento()),
                TIPO,
                new Coluna("TERMINAL A", 39.7, l -> l.item().terminalA()),
                new Coluna("DESP. A", 17.1, l -> l.item().desponteA()),
                new Coluna("TERMINAL B", 35.9, l -> l.item().terminalB()),
                new Coluna("DESP. B", 16.0, l -> l.item().desponteB())));
        return colunas;
    }

    private final CoresCabos cores;

    public ComunizadoPlanilha(CoresCabos cores) {
        this.cores = Objects.requireNonNull(cores);
    }

    public byte[] gerar(CabecalhoComunizado cabecalho, List<ItemCorte> itens) {
        Objects.requireNonNull(cabecalho, "O cabeçalho é obrigatório.");
        Objects.requireNonNull(itens, "A lista de circuitos não pode ser nula.");

        boolean comGravacao = itens.stream().anyMatch(i -> i.gravacao() != null && !i.gravacao().isBlank());
        List<Coluna> colunas = colunas(comGravacao);
        int ultimaColuna = colunas.size() - 1;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(ABA);
            Estilos estilos = new Estilos(workbook);

            for (int coluna = 0; coluna < colunas.size(); coluna++) {
                sheet.setColumnWidth(coluna, (int) Math.round(colunas.get(coluna).largura() * 256));
            }

            escreverBlocoCabecalho(sheet, cabecalho, ultimaColuna, estilos);
            escreverCabecalhoTabela(sheet, colunas, estilos);
            int ultimaLinha = escreverItens(sheet, colunas, cabecalho, itens, estilos);
            configurarImpressao(workbook, sheet, ultimaColuna, ultimaLinha, itens.size());

            try (var out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar o comunizado", e);
        }
    }

    // ---------- cabeçalho (linhas 1 a 8) ----------

    private void escreverBlocoCabecalho(Sheet sheet, CabecalhoComunizado c, int ultimaColuna, Estilos e) {
        // linha 1: logo (A1:C1) + título
        altura(sheet, 0, 57);
        mesclar(sheet, 0, 0, 2, null, e.caixa);
        inserirLogo(sheet);
        mesclar(sheet, 0, 3, ultimaColuna, "COMUNIZADO", e.titulo);

        // linhas 2, 4, 6 e 8: rótulo + valor à esquerda; rótulo + valor à direita
        campo(sheet, 1, "REALIZADO EM:", c.realizadoEm(), "DOC. REFERÊNCIA", c.docReferencia(), ultimaColuna, e);
        campo(sheet, 3, "PRODUTO:", c.produto(), "INÍCIO", c.inicio(), ultimaColuna, e);
        campo(sheet, 5, "QUANTIDADE:", c.quantidade(), "FIM", c.fim(), ultimaColuna, e);
        altura(sheet, 7, 18.75);
        rotuloEValor(sheet, 7, 0, "OP:", c.op(), e);

        // linhas de respiro entre os campos (como no modelo)
        altura(sheet, 1, 20.25);
        altura(sheet, 2, 7.5);
        altura(sheet, 4, 8.25);
        altura(sheet, 6, 8.25);
        altura(sheet, 8, 21);
    }

    private void campo(Sheet sheet, int linha, String rotuloEsq, Object valorEsq, String rotuloDir, Object valorDir,
            int ultimaColuna, Estilos e) {
        if (linha != 1) {
            altura(sheet, linha, 18.75);
        }
        rotuloEValor(sheet, linha, 0, rotuloEsq, valorEsq, e);
        celula(sheet, linha, 5, rotuloDir, e.rotulo);
        mesclar(sheet, linha, 6, ultimaColuna, valorDir, e.valor);
    }

    /** Rótulo na coluna A e valor mesclado em B:D. */
    private void rotuloEValor(Sheet sheet, int linha, int coluna, String rotulo, Object valor, Estilos e) {
        celula(sheet, linha, coluna, rotulo, e.rotulo);
        mesclar(sheet, linha, coluna + 1, coluna + 3, valor, e.valor);
    }

    /** Logo LANX Cables centralizada em A1:C1 (resources/producao/comunizado/logo.png). */
    private void inserirLogo(Sheet sheet) {
        try (InputStream in = ComunizadoPlanilha.class.getResourceAsStream(LOGO)) {
            if (in == null) {
                return; // sem a imagem, o cabeçalho sai só com o título
            }
            Workbook wb = sheet.getWorkbook();
            int imagem = wb.addPicture(in.readAllBytes(), Workbook.PICTURE_TYPE_PNG);
            // logo 314x112 px (2,8:1) com 66 px de altura → 185 px de largura, centralizada
            // em A:C (~442 px): começa a 128 px dentro de A (151 px) e termina a 162 px dentro de B
            ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setDx1(Units.pixelToEMU(128));
            anchor.setDy1(Units.pixelToEMU(5));
            anchor.setCol2(1);
            anchor.setRow2(0);
            anchor.setDx2(Units.pixelToEMU(162));
            anchor.setDy2(Units.pixelToEMU(71));
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
            sheet.createDrawingPatriarch().createPicture(anchor, imagem);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler a logo do comunizado", e);
        }
    }

    // ---------- tabela ----------

    private void escreverCabecalhoTabela(Sheet sheet, List<Coluna> colunas, Estilos e) {
        altura(sheet, LINHA_CABECALHO_TABELA, 35.1);
        for (int coluna = 0; coluna < colunas.size(); coluna++) {
            celula(sheet, LINHA_CABECALHO_TABELA, coluna, colunas.get(coluna).titulo(), e.cabecalhoTabela);
        }
    }

    private int escreverItens(Sheet sheet, List<Coluna> colunas, CabecalhoComunizado c, List<ItemCorte> itens,
            Estilos e) {
        int linha = LINHA_CABECALHO_TABELA;
        for (int i = 0; i < itens.size(); i++) {
            ItemCorte item = itens.get(i);
            linha++;
            altura(sheet, linha, 39.95);
            Linha dados = new Linha(
                    item,
                    CodigoCabo.ler(item.tipo(), cores),
                    texto(item.produto()).isBlank() ? c.produtoCurto() : texto(item.produto()),
                    texto(item.operador()).isBlank() ? texto(c.operador()) : texto(item.operador()));
            CellStyle estilo = e.linha(dados.cabo().map(CodigoCabo::cor).orElse(null));

            for (int coluna = 0; coluna < colunas.size(); coluna++) {
                Coluna definicao = colunas.get(coluna);
                CellStyle estiloCelula = definicao == TIPO ? e.tipo(dados.cabo(), estilo) : estilo;
                celula(sheet, linha, coluna, definicao.valor().apply(dados), estiloCelula);
            }

            // quebra de página a cada ITENS_POR_PAGINA itens (o cabeçalho se repete na próxima)
            if ((i + 1) % ITENS_POR_PAGINA == 0 && i + 1 < itens.size()) {
                sheet.setRowBreak(linha);
            }
        }
        return linha;
    }

    private void configurarImpressao(XSSFWorkbook workbook, Sheet sheet, int ultimaColuna, int ultimaLinha,
            int quantidadeItens) {
        int paginas = Math.max(1, (quantidadeItens + ITENS_POR_PAGINA - 1) / ITENS_POR_PAGINA);
        sheet.setDisplayGridlines(false);
        sheet.setZoom(70);
        sheet.setFitToPage(true);
        sheet.setMargin(Sheet.LeftMargin, 0.24);
        sheet.setMargin(Sheet.RightMargin, 0.24);
        sheet.setMargin(Sheet.TopMargin, 0.16);
        sheet.setMargin(Sheet.BottomMargin, 0.16);
        PrintSetup print = sheet.getPrintSetup();
        print.setLandscape(true);
        print.setPaperSize(PrintSetup.A4_PAPERSIZE);
        // uma página de largura e exatamente uma página por bloco de ITENS_POR_PAGINA itens
        print.setFitWidth((short) 1);
        print.setFitHeight((short) paginas);
        // o cabeçalho do comunizado (logo, campos e títulos da tabela) se repete em cada página
        sheet.setRepeatingRows(new CellRangeAddress(0, LINHA_CABECALHO_TABELA, -1, -1));
        sheet.getFooter().setRight("Pág. &P / &N");
        workbook.setPrintArea(0, 0, ultimaColuna, 0, ultimaLinha);
    }

    // ---------- valores ----------


    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // ---------- células ----------

    private static void altura(Sheet sheet, int linha, double pontos) {
        linha(sheet, linha).setHeightInPoints((float) pontos);
    }

    private static Row linha(Sheet sheet, int indice) {
        Row row = sheet.getRow(indice);
        return row != null ? row : sheet.createRow(indice);
    }

    private static void celula(Sheet sheet, int linha, int coluna, Object valor, CellStyle estilo) {
        Cell cell = linha(sheet, linha).createCell(coluna);
        cell.setCellStyle(estilo);
        if (valor instanceof LocalDate data) {
            cell.setCellValue(data);
        } else if (valor instanceof BigDecimal numero) {
            cell.setCellValue(numero.doubleValue());
        } else if (valor != null && !valor.toString().isBlank()) {
            cell.setCellValue(valor.toString());
        }
    }

    private static void mesclar(Sheet sheet, int linha, int primeira, int ultima, Object valor, CellStyle estilo) {
        for (int coluna = primeira; coluna <= ultima; coluna++) {
            celula(sheet, linha, coluna, coluna == primeira ? valor : null, estilo);
        }
        CellRangeAddress regiao = new CellRangeAddress(linha, linha, primeira, ultima);
        sheet.addMergedRegion(regiao);
        RegionUtil.setBorderTop(BorderStyle.THIN, regiao, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, regiao, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, regiao, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, regiao, sheet);
    }

    // ---------- estilos (fonte Calibri, bordas finas, centralizado, como no modelo) ----------

    private static final class Estilos {

        final CellStyle caixa;
        final CellStyle titulo;
        final CellStyle rotulo;
        final CellStyle valor;
        final CellStyle cabecalhoTabela;
        private final XSSFWorkbook wb;
        private final CellStyle linhaPadrao;
        private final Map<String, CellStyle> linhas = new HashMap<>();
        private final Map<String, XSSFFont> fontes = new HashMap<>();

        Estilos(XSSFWorkbook wb) {
            this.wb = wb;
            caixa = base(wb, fonte(wb, 11, false, null), false);
            titulo = base(wb, fonte(wb, 26, true, null), false);
            rotulo = base(wb, fonte(wb, 16, false, null), false);
            valor = base(wb, fonte(wb, 16, false, null), false);
            valor.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy"));
            cabecalhoTabela = base(wb, fonte(wb, 16, false, null), true);
            linhaPadrao = base(wb, fonte(wb, 18, false, null), false);
        }

        /** Estilo da linha pela cor do cabo (uma instância por cor: o Excel limita estilos). */
        CellStyle linha(CorCabo cor) {
            if (cor == null) {
                return linhaPadrao;
            }
            return linhas.computeIfAbsent(cor.codigo(), k -> {
                XSSFCellStyle estilo = base(wb, fonteLinha(cor), false);
                if (cor.fundo() != null) {
                    estilo.setFillForegroundColor(new XSSFColor(rgb(cor.fundo())));
                    estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }
                return estilo;
            });
        }

        XSSFFont fonteLinha(CorCabo cor) {
            return fontes.computeIfAbsent(cor.codigo(), k -> fonte(wb, 18, cor.negrito(), cor.texto()));
        }

        /**
         * Célula TIPO: cabo bicolor ganha gradiente cor → faixa (clareado para o texto
         * aparecer) com o texto em preto negrito; cabo de uma cor usa o estilo da linha.
         */
        CellStyle tipo(Optional<CodigoCabo> cabo, CellStyle estiloDaLinha) {
            if (cabo.isEmpty() || cabo.get().faixa() == null) {
                return estiloDaLinha;
            }
            CorCabo cor = cabo.get().cor();
            CorCabo faixa = cabo.get().faixa();
            return linhas.computeIfAbsent("gradiente:" + cor.codigo() + ":" + faixa.codigo(), k -> {
                XSSFCellStyle estilo = base(wb, fonteTipoBicolor(), false);
                PreenchimentoGradiente.aplicar(estilo, wb.getStylesSource(),
                        PreenchimentoGradiente.clarear(corDoCabo(cor), CLAREAR_GRADIENTE),
                        PreenchimentoGradiente.clarear(corDoCabo(faixa), CLAREAR_GRADIENTE));
                return estilo;
            });
        }

        /** Cor "real" do cabo para o gradiente: a de faixa (branco vira cinza claro, amarelo forte). */
        private static String corDoCabo(CorCabo cor) {
            return cor.faixa();
        }

        private XSSFFont fonteTipoBicolor() {
            return fontes.computeIfAbsent("tipo-bicolor", k -> fonte(wb, 18, true, "000000"));
        }

        private static XSSFCellStyle base(XSSFWorkbook wb, XSSFFont fonte, boolean quebraTexto) {
            XSSFCellStyle estilo = wb.createCellStyle();
            estilo.setFont(fonte);
            estilo.setAlignment(HorizontalAlignment.CENTER);
            estilo.setVerticalAlignment(VerticalAlignment.CENTER);
            estilo.setWrapText(quebraTexto);
            // códigos longos ("TMFDTMNS00BT0715(TR02)") encolhem em vez de vazar da célula
            estilo.setShrinkToFit(!quebraTexto);
            estilo.setBorderTop(BorderStyle.THIN);
            estilo.setBorderBottom(BorderStyle.THIN);
            estilo.setBorderLeft(BorderStyle.THIN);
            estilo.setBorderRight(BorderStyle.THIN);
            return estilo;
        }

        private static XSSFFont fonte(XSSFWorkbook wb, int tamanho, boolean negrito, String rgb) {
            XSSFFont fonte = wb.createFont();
            fonte.setFontName(FONTE);
            fonte.setFontHeightInPoints((short) tamanho);
            fonte.setBold(negrito);
            if (rgb != null) {
                fonte.setColor(new XSSFColor(rgb(rgb)));
            }
            return fonte;
        }

        private static byte[] rgb(String hex) {
            return new byte[] {
                    (byte) Integer.parseInt(hex.substring(0, 2), 16),
                    (byte) Integer.parseInt(hex.substring(2, 4), 16),
                    (byte) Integer.parseInt(hex.substring(4, 6), 16) };
        }
    }
}
