
package br.lanx.api.producao.operacoes.comunizado;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import br.lanx.api.producao.operacoes.comunizado.domain.CabecalhoComunizado;
import br.lanx.api.producao.operacoes.comunizado.domain.CodigoCabo;
import br.lanx.api.producao.operacoes.comunizado.domain.CoresCabos;
import br.lanx.api.producao.operacoes.comunizado.domain.Desponte;
import br.lanx.api.producao.operacoes.comunizado.domain.ItemCorte;

@Service
public class ComunizadoService {

    /** Nomes de coluna das ITs mais antigas → nome usado aqui ("CIR" = "CIRC"). */
    private static final Map<String, String> SINONIMOS = Map.of("CIR", "CIRC");

    /** Além destas, a tabela precisa de CIRC (ou CIR) ou GRAVAÇÃO (que traz o circuito no fim: "XP1:5 / 13"). */
    private static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "COMP",
            "TIPO",
            "TERMINALA",
            "DESPA",
            "TERMINALB",
            "DESPB");

    private final CoresCabos cores = CoresCabos.padrao();
    private final ComunizadoPlanilha comunicadoPlanilha = new ComunizadoPlanilha(cores);

    /** Planilha gerada e o nome do arquivo ("KHN321 000 000 dia 20-10-2026.xlsx"). */
    public record Resultado(String nomeArquivo, byte[] conteudo) {
    }

    public Resultado executar(MultipartFile arquivo, ComunizadoRequest request) {
        validarArquivo(arquivo);

        List<ItemCorte> itens;
        DadosIt dadosIt;

        try (InputStream inputStream = arquivo.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw erro("A planilha não possui abas.");
            }

            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

            FormulaEvaluator evaluator = workbook
                    .getCreationHelper()
                    .createFormulaEvaluator();

            // A lista de corte pode continuar em outras abas ("IT CABOS", "IT CABOS (2)"...):
            // lê toda aba que tenha a tabela de cabos, na ordem das abas. "IT TUBOS" não tem
            // as colunas de terminal e fica de fora.
            itens = new ArrayList<>();
            dadosIt = null;
            for (Sheet sheet : workbook) {
                int linhaCabecalho = localizarCabecalho(sheet, formatter, evaluator);
                if (linhaCabecalho < 0) {
                    continue;
                }
                Map<String, Integer> colunas = mapearColunas(sheet.getRow(linhaCabecalho), formatter, evaluator);
                if (dadosIt == null) {
                    dadosIt = lerCabecalhoIt(sheet, linhaCabecalho, formatter, evaluator);
                }
                try {
                    itens.addAll(lerItens(sheet, linhaCabecalho, colunas, formatter, evaluator));
                } catch (ResponseStatusException e) {
                    throw erro("Aba '" + sheet.getSheetName() + "': " + e.getReason());
                }
            }

            if (dadosIt == null) {
                throw erro("Não foi possível localizar a tabela de cabos em nenhuma aba. "
                        + "Verifique as colunas CIRC (ou GRAVAÇÃO), COMP, TIPO, TERMINAL A, "
                        + "DESP. A, TERMINAL B e DESP. B.");
            }

        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível ler o arquivo Excel.",
                    exception);
        } catch (RuntimeException exception) {
            if (exception instanceof ResponseStatusException) {
                throw exception;
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O arquivo não é uma planilha Excel válida ou contém dados inválidos.",
                    exception);
        }

        if (itens.isEmpty()) {
            throw erro("A planilha não contém circuitos para comunicar.");
        }

        itens.sort(comparadorCircuitos());

        CabecalhoComunizado cabecalho = montarCabecalho(dadosIt, request);
        String nome = (cabecalho.produto().isBlank() ? "Comunizado" : cabecalho.produto())
                + " dia " + cabecalho.realizadoEm().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx";
        return new Resultado(nome, comunicadoPlanilha.gerar(cabecalho, itens));
    }

    /** O que a IT traz no bloco acima da tabela: código do produto e data da revisão. */
    private record DadosIt(String produto, LocalDate docReferencia) {
    }

    /**
     * Lê os campos do cabeçalho da IT pelo rótulo (o valor fica na linha de baixo,
     * na mesma coluna): "Código" e "Data". Há duas colunas "Data" (emissão e
     * revisão): vale a última preenchida, que é a revisão mais recente.
     */
    private DadosIt lerCabecalhoIt(
            Sheet sheet,
            int linhaCabecalho,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        String produto = null;
        LocalDate data = null;

        for (int indice = 0; indice < linhaCabecalho; indice++) {
            Row row = sheet.getRow(indice);
            Row abaixo = sheet.getRow(indice + 1);
            if (row == null || abaixo == null) {
                continue;
            }
            for (int coluna = 0; coluna < row.getLastCellNum(); coluna++) {
                String rotulo = normalizar(lerTexto(row.getCell(coluna), formatter, evaluator));
                Cell valor = abaixo.getCell(coluna);
                if (rotulo.equals("CODIGO") && produto == null) {
                    String texto = lerTexto(valor, formatter, evaluator);
                    produto = texto.isBlank() ? null : texto;
                } else if (rotulo.equals("DATA")) {
                    LocalDate lida = lerData(valor, evaluator);
                    if (lida != null) {
                        data = lida;
                    }
                }
            }
        }
        return new DadosIt(produto, data);
    }

    private LocalDate lerData(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        try {
            CellType tipo = cell.getCellType() == CellType.FORMULA
                    ? evaluator.evaluateFormulaCell(cell)
                    : cell.getCellType();
            if (tipo == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
        } catch (RuntimeException e) {
            // célula com data inválida: tratada como ausente
        }
        return null;
    }

    /** Requisição > IT; sem produto em nenhum dos dois não há como identificar o comunizado. */
    private CabecalhoComunizado montarCabecalho(DadosIt it, ComunizadoRequest request) {
        String produto = textoOu(request.produto(), it.produto());
        if (produto.isBlank()) {
            throw erro("A IT não tem o código do produto: informe o campo produto na requisição.");
        }
        return new CabecalhoComunizado(
                produto,
                request.docReferencia() != null ? request.docReferencia() : it.docReferencia(),
                request.realizadoEm() != null ? request.realizadoEm() : LocalDate.now(),
                quantidade(request.quantidade()),
                textoOu(request.op(), null),
                textoOu(request.inicio(), null),
                textoOu(request.fim(), null),
                textoOu(request.operador(), null));
    }

    /** "74" → "74 PÇS"; texto livre ("74 PÇS", "2 CX") fica como veio. */
    private static String quantidade(String valor) {
        String texto = textoOu(valor, null);
        if (texto.matches("\\d+")) {
            return texto + " PÇS";
        }
        return texto;
    }

    private static String textoOu(String preferido, String alternativa) {
        if (preferido != null && !preferido.isBlank()) {
            return preferido.trim();
        }
        return alternativa == null ? "" : alternativa.trim();
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw erro("Envie um arquivo Excel preenchido.");
        }

        String nome = arquivo.getOriginalFilename();

        if (nome == null
                || !(nome.toLowerCase(Locale.ROOT).endsWith(".xlsx")
                        || nome.toLowerCase(Locale.ROOT).endsWith(".xls"))) {
            throw erro("O arquivo deve estar no formato .xlsx ou .xls.");
        }
    }

    private int localizarCabecalho(
            Sheet sheet,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        // Procura o cabeçalho nas primeiras 15 linhas.
        int limite = Math.min(sheet.getLastRowNum(), 14);

        for (int indice = 0; indice <= limite; indice++) {
            Row row = sheet.getRow(indice);

            if (row == null) {
                continue;
            }

            Map<String, Integer> colunas = mapearColunas(
                    row,
                    formatter,
                    evaluator);

            if (colunas.keySet().containsAll(COLUNAS_OBRIGATORIAS)
                    && (colunas.containsKey("CIRC") || colunas.containsKey("GRAVACAO"))) {
                return indice;
            }
        }

        return -1; // aba sem a tabela de cabos (ex.: IT TUBOS)
    }

    private Map<String, Integer> mapearColunas(
            Row row,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        Map<String, Integer> colunas = new HashMap<>();

        if (row == null) {
            return colunas;
        }

        for (int indice = 0; indice < row.getLastCellNum(); indice++) {
            Cell cell = row.getCell(indice);

            String nomeColuna = normalizar(
                    lerTexto(cell, formatter, evaluator));

            if (!nomeColuna.isBlank()) {
                colunas.putIfAbsent(SINONIMOS.getOrDefault(nomeColuna, nomeColuna), indice);
            }
        }

        return colunas;
    }

    private List<ItemCorte> lerItens(
            Sheet sheet,
            int linhaCabecalho,
            Map<String, Integer> colunas,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        List<ItemCorte> itens = new ArrayList<>();
        Map<CellAddress, CellAddress> mesclagens = mesclagensVerticais(sheet, linhaCabecalho);

        for (int indice = linhaCabecalho + 1; indice <= sheet.getLastRowNum(); indice++) {

            Row row = sheet.getRow(indice);

            if (row == null || linhaVazia(row, formatter, evaluator)) {
                continue;
            }

            // a IT termina com notas e legenda: dali em diante não há mais circuitos
            if (inicioDoRodape(sheet, row, colunas, formatter, evaluator)) {
                break;
            }

            int numeroLinha = indice + 1;

            String gravacao = lerCampoTexto(
                    sheet, mesclagens, row, colunas, "GRAVACAO", formatter, evaluator);

            String circuito = colunas.containsKey("CIRC")
                    ? lerCampoTexto(sheet, mesclagens, row, colunas, "CIRC", formatter, evaluator)
                    : circuitoDaGravacao(gravacao);

            String comprimentoTexto = lerCampoTexto(
                    sheet, mesclagens, row, colunas, "COMP", formatter, evaluator);

            String tipo = lerCampoTexto(
                    sheet, mesclagens, row, colunas, "TIPO", formatter, evaluator);

            String terminalA = lerCampoTexto(
                    sheet, mesclagens, row, colunas, "TERMINALA", formatter, evaluator);

            String terminalB = lerCampoTexto(
                    sheet, mesclagens, row, colunas, "TERMINALB", formatter, evaluator);

            validarCampoObrigatorio(circuito, colunas.containsKey("CIRC") ? "CIRC" : "GRAVAÇÃO", numeroLinha);
            validarCampoObrigatorio(comprimentoTexto, "COMP", numeroLinha);
            validarCampoObrigatorio(tipo, "TIPO", numeroLinha);
            validarCampoObrigatorio(terminalA, "TERMINAL A", numeroLinha);
            validarCampoObrigatorio(terminalB, "TERMINAL B", numeroLinha);

            BigDecimal comprimento = lerDecimal(
                    celula(sheet, mesclagens, row, colunas.get("COMP")),
                    formatter,
                    evaluator,
                    true,
                    numeroLinha);

            if (comprimento.signum() < 0) {
                throw erro("COMP não pode ser negativo na linha " + numeroLinha + ".");
            }

            String desponteA = lerDesponte(
                    celula(sheet, mesclagens, row, colunas.get("DESPA")), "DESP. A", formatter, evaluator, numeroLinha);

            String desponteB = lerDesponte(
                    celula(sheet, mesclagens, row, colunas.get("DESPB")), "DESP. B", formatter, evaluator, numeroLinha);

            String produto = lerCampoTexto(
                    sheet, mesclagens, row, colunas, "PROD", formatter, evaluator);

            String operador = lerCampoTexto(
                    sheet, mesclagens, row, colunas, "OPERADOR", formatter, evaluator);

            itens.add(new ItemCorte(
                    produto,
                    operador,
                    circuito,
                    gravacao.isBlank() ? null : gravacao,
                    comprimento,
                    tipo,
                    terminalA,
                    desponteA,
                    terminalB,
                    desponteB));
        }

        return itens;
    }

    /** "31MP1-XP1:1 / 01" → "01": o número do circuito vem depois da última barra. */
    static String circuitoDaGravacao(String gravacao) {
        if (gravacao == null || gravacao.isBlank()) {
            return "";
        }
        int barra = gravacao.lastIndexOf('/');
        String numero = barra < 0 ? gravacao : gravacao.substring(barra + 1);
        return numero.trim();
    }

    private static final List<String> MARCADORES_RODAPE = List.of(
            "NOTA", "LEGENDA", "OBS", "LOCALIZACAO", "TOLERANCIA");

    /**
     * Rodapé da IT: linha mesclada ocupando boa parte da largura (ex.: "NOTA 1: ..."
     * em A32:L32) ou texto começando por NOTA/LEGENDA/LOCALIZAÇÃO/TOLERÂNCIA em
     * qualquer célula fora da coluna OBS. Só o rodapé encerra a tabela: a quantidade
     * de linhas preenchidas por aba (22 nas ITs atuais) não é usada, para não perder
     * circuitos. Linha incompleta antes do rodapé vira erro, não é descartada.
     */
    private boolean inicioDoRodape(
            Sheet sheet,
            Row row,
            Map<String, Integer> colunas,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        int linha = row.getRowNum();
        for (CellRangeAddress regiao : sheet.getMergedRegions()) {
            if (regiao.getFirstRow() <= linha && linha <= regiao.getLastRow()
                    && regiao.getLastColumn() - regiao.getFirstColumn() + 1 >= Math.max(4, colunas.size() / 2)) {
                return true;
            }
        }
        // a coluna OBS dos circuitos referencia notas ("NOTA 1"): não é rodapé
        Integer colunaObs = colunas.get("OBS");
        for (int indice = 0; indice < row.getLastCellNum(); indice++) {
            if (colunaObs != null && indice == colunaObs) {
                continue;
            }
            String texto = normalizar(lerTexto(row.getCell(indice), formatter, evaluator));
            if (MARCADORES_RODAPE.stream().anyMatch(texto::startsWith)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ordem do comunizado: mesmo modelo de cabo, bitola (menor primeiro) e cor
     * (na ordem de cores-cabos.csv) juntos; dentro disso, maior comprimento
     * primeiro, terminais e circuito. Tipos fora do padrão vão para o fim.
     */
    private Comparator<ItemCorte> comparadorCircuitos() {
        Comparator<Optional<CodigoCabo>> porCabo = (a, b) -> {
            if (a.isPresent() && b.isPresent()) {
                return CodigoCabo.ORDEM.compare(a.get(), b.get());
            }
            return Boolean.compare(a.isEmpty(), b.isEmpty());
        };
        return Comparator
                .comparing((ItemCorte item) -> CodigoCabo.ler(item.tipo(), cores), porCabo)
                .thenComparing(ItemCorte::tipo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(ItemCorte::comprimento, Comparator.nullsLast(Comparator.<BigDecimal>reverseOrder()))
                .thenComparing(ItemCorte::terminalA, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(ItemCorte::terminalB, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(ItemCorte::circuito, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Células mescladas na vertical dentro da tabela (ex.: COMP em B7:B8, compartilhado
     * pelo par trançado -AM/-VD): cada célula "escondida" aponta para a do canto
     * superior esquerdo, que é a única com valor.
     */
    private Map<CellAddress, CellAddress> mesclagensVerticais(Sheet sheet, int linhaCabecalho) {
        Map<CellAddress, CellAddress> mapa = new HashMap<>();
        for (CellRangeAddress regiao : sheet.getMergedRegions()) {
            if (regiao.getFirstRow() <= linhaCabecalho || regiao.getFirstRow() == regiao.getLastRow()) {
                continue;
            }
            CellAddress origem = new CellAddress(regiao.getFirstRow(), regiao.getFirstColumn());
            for (int linha = regiao.getFirstRow(); linha <= regiao.getLastRow(); linha++) {
                for (int coluna = regiao.getFirstColumn(); coluna <= regiao.getLastColumn(); coluna++) {
                    mapa.put(new CellAddress(linha, coluna), origem);
                }
            }
        }
        return mapa;
    }

    /** Célula da coluna nesta linha; se ela fizer parte de uma mesclagem, a célula com o valor. */
    private Cell celula(Sheet sheet, Map<CellAddress, CellAddress> mesclagens, Row row, Integer coluna) {
        if (coluna == null) {
            return null;
        }
        CellAddress origem = mesclagens.get(new CellAddress(row.getRowNum(), coluna));
        if (origem == null) {
            return row.getCell(coluna);
        }
        Row linhaOrigem = sheet.getRow(origem.getRow());
        return linhaOrigem == null ? null : linhaOrigem.getCell(origem.getColumn());
    }

    private String lerCampoTexto(
            Sheet sheet,
            Map<CellAddress, CellAddress> mesclagens,
            Row row,
            Map<String, Integer> colunas,
            String coluna,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        return lerTexto(celula(sheet, mesclagens, row, colunas.get(coluna)), formatter, evaluator).trim();
    }

    /** Número ("6.0") ou texto com uma ou duas medidas ("40.0/6.0"), normalizado por {@link Desponte}. */
    private String lerDesponte(
            Cell cell,
            String campo,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int numeroLinha) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        CellType tipo = cell.getCellType() == CellType.FORMULA ? evaluator.evaluateFormulaCell(cell) : cell.getCellType();
        // Numérico pelo valor (o formato de exibição pode esconder casas); texto como veio
        String bruto = tipo == CellType.NUMERIC
                ? BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString()
                : lerTexto(cell, formatter, evaluator);
        try {
            return Desponte.normalizar(bruto);
        } catch (IllegalArgumentException e) {
            throw erro(campo + " inválido na linha " + numeroLinha + ": " + bruto
                    + ". Use um número (6.0) ou duas medidas separadas por barra (40.0/6.0).");
        }
    }

    private String lerTexto(
            Cell cell,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell, evaluator).trim();
    }

    private BigDecimal lerDecimal(
            Cell cell,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            boolean comprimentoInteiro,
            int numeroLinha) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue())
                        .stripTrailingZeros();
            }

            if (cell.getCellType() == CellType.FORMULA) {
                var resultado = evaluator.evaluate(cell);

                if (resultado != null
                        && resultado.getCellType() == CellType.NUMERIC) {
                    return BigDecimal.valueOf(resultado.getNumberValue())
                            .stripTrailingZeros();
                }
            }

            String valor = lerTexto(cell, formatter, evaluator)
                    .replaceAll("\\s+", "");

            if (valor.isBlank()) {
                return null;
            }

            // Comprimentos digitados como "22.400" podem representar
            // 22400 na notação brasileira.
            if (comprimentoInteiro
                    && valor.matches("\\d{1,3}(\\.\\d{3})+")) {
                valor = valor.replace(".", "");
            } else if (valor.contains(",")) {
                // Exemplo: "3.500,5" -> "3500.5".
                valor = valor.replace(".", "")
                        .replace(",", ".");
            }

            return new BigDecimal(valor).stripTrailingZeros();

        } catch (NumberFormatException exception) {
            throw erro(
                    "Valor numérico inválido na linha "
                            + numeroLinha + ": "
                            + lerTexto(cell, formatter, evaluator));
        }
    }

    private boolean linhaVazia(
            Row row,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        for (int indice = 0; indice < row.getLastCellNum(); indice++) {
            if (!lerTexto(
                    row.getCell(indice),
                    formatter,
                    evaluator).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private void validarCampoObrigatorio(
            String valor,
            String campo,
            int numeroLinha) {
        if (valor == null || valor.isBlank()) {
            throw erro(
                    "O campo " + campo
                            + " está vazio na linha " + numeroLinha + ".");
        }
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(
                valor,
                Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
    }

    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                mensagem);
    }
}
