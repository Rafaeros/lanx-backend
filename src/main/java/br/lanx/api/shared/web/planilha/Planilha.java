package br.lanx.api.shared.web.planilha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Pequeno construtor de planilhas .xlsx (Apache POI) com os estilos dos
 * relatórios: cabeçalho destacado, datas dd/MM/yyyy, moeda, percentual,
 * filtro automático, cabeçalho congelado e a logo LANX.
 */
public final class Planilha implements AutoCloseable {

    public static final MediaType XLSX = MediaType
            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final CellStyle titulo;
    private final CellStyle subtitulo;
    private final CellStyle cabecalho;
    private final CellStyle texto;
    private final CellStyle destaque;
    private final CellStyle data;
    private final CellStyle numero;
    private final CellStyle codigo;
    private final CellStyle decimal;
    private final CellStyle moeda;
    private final CellStyle percentual;
    private final CellStyle dataAlerta;
    private final CellStyle moedaTotal;

    private static final float ALTURA_LOGO_PT = 34f;

    /** public/brand/logo.png do frontend, copiada para src/main/resources/brand. */
    private static final byte[] LOGO = carregarLogo();

    public Planilha() {
        var format = workbook.createDataFormat();

        Font negrito = workbook.createFont();
        negrito.setBold(true);

        Font fonteTitulo = workbook.createFont();
        fonteTitulo.setBold(true);
        fonteTitulo.setFontHeightInPoints((short) 14);

        Font fonteCabecalho = workbook.createFont();
        fonteCabecalho.setBold(true);
        fonteCabecalho.setColor(IndexedColors.WHITE.getIndex());

        titulo = workbook.createCellStyle();
        titulo.setFont(fonteTitulo);

        subtitulo = workbook.createCellStyle();
        Font fonteSubtitulo = workbook.createFont();
        fonteSubtitulo.setItalic(true);
        fonteSubtitulo.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        subtitulo.setFont(fonteSubtitulo);

        cabecalho = workbook.createCellStyle();
        cabecalho.setFont(fonteCabecalho);
        cabecalho.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        cabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cabecalho.setAlignment(HorizontalAlignment.CENTER);
        cabecalho.setBorderBottom(BorderStyle.THIN);
        cabecalho.setWrapText(true);

        texto = workbook.createCellStyle();
        texto.setVerticalAlignment(VerticalAlignment.CENTER);
        destaque = workbook.createCellStyle();
        destaque.setFont(negrito);
        destaque.setVerticalAlignment(VerticalAlignment.CENTER);
        data = estilo(format.getFormat("dd/mm/yyyy"));
        numero = estilo(format.getFormat("#,##0"));
        codigo = estilo(format.getFormat("0"));
        decimal = estilo(format.getFormat("#,##0.00##"));
        moeda = estilo(format.getFormat("\"R$\" #,##0.00"));
        percentual = estilo(format.getFormat("0.00\"%\""));

        Font fonteAlerta = workbook.createFont();
        fonteAlerta.setBold(true);
        fonteAlerta.setColor(IndexedColors.RED.getIndex());
        dataAlerta = estilo(format.getFormat("dd/mm/yyyy"));
        dataAlerta.setFont(fonteAlerta);

        moedaTotal = estilo(format.getFormat("\"R$\" #,##0.00"));
        moedaTotal.setFont(negrito);
        moedaTotal.setBorderTop(BorderStyle.THIN);
    }

    private static byte[] carregarLogo() {
        try (InputStream in = Planilha.class.getResourceAsStream("/brand/logo.png")) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    private CellStyle estilo(short dataFormat) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(dataFormat);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    public XSSFWorkbook workbook() {
        return workbook;
    }

    public XSSFSheet aba(String nome) {
        return workbook.createSheet(nome);
    }

    /** Título (linha 0) e subtítulo (linha 1). Devolve a próxima linha livre. */
    public int titulo(Sheet sheet, String titulo, String subtitulo) {
        Cell t = sheet.createRow(0).createCell(0);
        t.setCellValue(titulo);
        t.setCellStyle(this.titulo);
        Cell s = sheet.createRow(1).createCell(0);
        s.setCellValue(subtitulo);
        s.setCellStyle(this.subtitulo);
        return 3;
    }

    /** Linha de cabeçalho com filtro automático e congelada no topo. */
    public void cabecalho(Sheet sheet, int linha, List<String> colunas, int ultimaLinhaDados) {
        Row row = sheet.createRow(linha);
        row.setHeightInPoints(30);
        for (int i = 0; i < colunas.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(colunas.get(i));
            cell.setCellStyle(cabecalho);
        }
        sheet.createFreezePane(0, linha + 1);
        if (ultimaLinhaDados > linha) {
            sheet.setAutoFilter(new CellRangeAddress(linha, ultimaLinhaDados, 0, colunas.size() - 1));
        }
    }

    /**
     * Logo LANX na linha do título (que fica mais alta para caber), a partir da
     * coluna informada. Chame depois de {@link #larguras}: a largura da imagem
     * é distribuída pelas colunas.
     */
    public void logo(XSSFSheet sheet, int coluna) {
        if (LOGO == null) {
            return;
        }
        Row titulo = sheet.getRow(0) != null ? sheet.getRow(0) : sheet.createRow(0);
        titulo.setHeightInPoints(ALTURA_LOGO_PT);

        int imagem = workbook.addPicture(LOGO, Workbook.PICTURE_TYPE_PNG);
        XSSFClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        anchor.setCol1(coluna);
        anchor.setRow1(0);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
        XSSFPicture logo = sheet.createDrawingPatriarch().createPicture(anchor, imagem);

        // o resize do POI acerta a largura, mas não a altura de imagens menores que
        // a linha: a altura é fixada à mão, ocupando a linha do título inteira
        double alturaPx = ALTURA_LOGO_PT * Units.PIXEL_DPI / Units.POINT_DPI;
        logo.resize(alturaPx / logo.getImageDimension().getHeight());
        int alturaEmu = (int) (ALTURA_LOGO_PT * Units.EMU_PER_POINT);
        anchor.setRow2(0);
        anchor.setDy2(alturaEmu);
        logo.getCTPicture().getSpPr().getXfrm().getExt().setCy(alturaEmu);
    }

    /** Impressão em A4 paisagem, ajustada à largura, repetindo a linha de cabeçalho em cada página. */
    public void paisagem(Sheet sheet, int linhaCabecalho) {
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.setRepeatingRows(new CellRangeAddress(linhaCabecalho, linhaCabecalho, -1, -1));
    }

    public void larguras(Sheet sheet, int... caracteres) {
        for (int i = 0; i < caracteres.length; i++) {
            sheet.setColumnWidth(i, Math.min(255, caracteres[i]) * 256);
        }
    }

    public void texto(Row row, int col, String value) {
        if (value != null && !value.isBlank()) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value);
            cell.setCellStyle(texto);
        }
    }

    public void negrito(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(destaque);
    }

    public void data(Row row, int col, LocalDate value) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value);
            cell.setCellStyle(data);
        }
    }

    /** Data em vermelho e negrito (ex.: previsão vencida). */
    public void data(Row row, int col, LocalDate value, boolean alerta) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value);
            cell.setCellStyle(alerta ? dataAlerta : data);
        }
    }

    /** Número que é identificador (ex.: negociação 5335): sem separador de milhar. */
    public void codigo(Row row, int col, Number value) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(codigo);
        }
    }

    public void inteiro(Row row, int col, Number value) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(numero);
        }
    }

    public void decimal(Row row, int col, BigDecimal value) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(decimal);
        }
    }

    public void moeda(Row row, int col, BigDecimal value) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(moeda);
        }
    }

    /** Total em moeda por fórmula (ex.: SUBTOTAL, que respeita o filtro automático). */
    public void moedaTotal(Row row, int col, String formula) {
        Cell cell = row.createCell(col);
        cell.setCellFormula(formula);
        cell.setCellStyle(moedaTotal);
    }

    /** Percentual já em base 100 (ex.: 84.5 → "84,50%"). */
    public void percentual(Row row, int col, BigDecimal value) {
        if (value != null) {
            Cell cell = row.createCell(col);
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(percentual);
        }
    }

    public byte[] toBytes() {
        try (var out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar a planilha", e);
        }
    }

    @Override
    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Resposta de download com o nome do arquivo (UTF-8 no Content-Disposition). */
    public static ResponseEntity<byte[]> download(String nomeArquivo, byte[] conteudo) {
        var disposition = ContentDisposition.attachment().filename(nomeArquivo, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentLength(conteudo.length)
                .body(conteudo);
    }
}
