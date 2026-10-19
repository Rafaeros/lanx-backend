package br.lanx.api.shared.web.planilha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Pequeno construtor de planilhas .xlsx (Apache POI) com os estilos dos
 * relatórios: cabeçalho destacado, datas dd/MM/yyyy, moeda, percentual,
 * filtro automático e cabeçalho congelado.
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
    private final CellStyle decimal;
    private final CellStyle moeda;
    private final CellStyle percentual;

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
        destaque = workbook.createCellStyle();
        destaque.setFont(negrito);
        data = estilo(format.getFormat("dd/mm/yyyy"));
        numero = estilo(format.getFormat("#,##0"));
        decimal = estilo(format.getFormat("#,##0.00##"));
        moeda = estilo(format.getFormat("\"R$\" #,##0.00"));
        percentual = estilo(format.getFormat("0.00\"%\""));
    }

    private CellStyle estilo(short dataFormat) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(dataFormat);
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
