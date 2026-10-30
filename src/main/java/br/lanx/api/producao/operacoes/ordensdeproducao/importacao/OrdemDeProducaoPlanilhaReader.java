package br.lanx.api.producao.operacoes.ordensdeproducao.importacao;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.LinhaRejeitada;

/**
 * Lê a planilha de OPs exportada do ERP (primeira aba, cabeçalho na linha 1).
 * As colunas são achadas pelo nome do cabeçalho, não pela posição; nomes
 * repetidos ("Código Insumo") valem pela primeira ocorrência. Não acessa o
 * banco: só transforma o arquivo em linhas válidas e rejeitadas.
 */
final class OrdemDeProducaoPlanilhaReader {

    static final String CODIGO = "Código";
    static final String CLIENTE = "Cliente";
    static final String MATERIAL_CODIGO = "Cód. Material";
    static final String MATERIAL = "Material";
    static final String QUANTIDADE = "Quantidade";
    static final String ENTREGA = "Entrega";
    /** Opcional: sem ela, todas as linhas contam como em aberto. */
    static final String STATUS = "Status";

    /** O ERP às vezes anota texto junto ao código ("OP-0007174   - usado op kanban 6268"). */
    private static final Pattern CODIGO_OP = Pattern.compile("OP-\\d+", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int CODIGO_MAX = 10;
    private static final int CLIENTE_MAX = 250;
    private static final List<String> OBRIGATORIOS = List.of(CODIGO, CLIENTE, MATERIAL_CODIGO, MATERIAL, QUANTIDADE,
            ENTREGA);

    record Leitura(List<LinhaOrdemImportada> linhas, List<LinhaRejeitada> rejeitadas) {
    }

    private OrdemDeProducaoPlanilhaReader() {
    }

    static Leitura ler(InputStream in) throws IOException {
        try (Workbook wb = abrir(in)) {
            Sheet aba = wb.getSheetAt(0);
            Row cabecalho = aba.getRow(aba.getFirstRowNum());
            if (cabecalho == null) {
                throw new PlanilhaOrdensInvalidasException("A planilha está vazia.");
            }
            var fmt = new DataFormatter(Locale.forLanguageTag("pt-BR"));
            Map<String, Integer> col = colunas(cabecalho, fmt);
            var faltando = OBRIGATORIOS.stream().filter(c -> !col.containsKey(c)).toList();
            if (!faltando.isEmpty()) {
                throw new PlanilhaOrdensInvalidasException("Colunas ausentes na planilha: " + String.join(", ", faltando)
                        + ". Use a exportação de ordens de produção do ERP.");
            }

            List<LinhaOrdemImportada> linhas = new ArrayList<>();
            List<LinhaRejeitada> rejeitadas = new ArrayList<>();
            for (int i = cabecalho.getRowNum() + 1; i <= aba.getLastRowNum(); i++) {
                Row row = aba.getRow(i);
                if (row == null || vazia(row, fmt)) {
                    continue;
                }
                ler(row, col, fmt, linhas, rejeitadas);
            }
            return new Leitura(linhas, rejeitadas);
        }
    }

    private static Workbook abrir(InputStream in) {
        try {
            return WorkbookFactory.create(in);
        } catch (IllegalArgumentException | IOException e) {
            // arquivo vazio (EmptyFileException) ou que não é planilha: POI lança um destes
            throw new PlanilhaOrdensInvalidasException("O arquivo não é uma planilha Excel (.xlsx) válida.");
        }
    }

    private static void ler(Row row, Map<String, Integer> col, DataFormatter fmt, List<LinhaOrdemImportada> linhas,
            List<LinhaRejeitada> rejeitadas) {
        int numero = row.getRowNum() + 1;
        String bruto = texto(row, col.get(CODIGO), fmt);
        String codigo = codigo(bruto);
        String cliente = texto(row, col.get(CLIENTE), fmt);
        String materialCodigo = texto(row, col.get(MATERIAL_CODIGO), fmt);
        String material = texto(row, col.get(MATERIAL), fmt);
        BigDecimal quantidade = numero(row.getCell(col.get(QUANTIDADE)), fmt);
        LocalDate entrega = data(row.getCell(col.get(ENTREGA)));
        String status = col.containsKey(STATUS) ? texto(row, col.get(STATUS), fmt) : null;

        String motivo = null;
        if (codigo == null) {
            motivo = "Código da OP inválido: \"" + (bruto == null ? "" : bruto) + "\" (esperado OP-0000000)";
        } else if (codigo.length() > CODIGO_MAX) {
            motivo = "Código da OP com mais de " + CODIGO_MAX + " caracteres";
        } else if (cliente == null) {
            motivo = "Cliente vazio";
        } else if (cliente.length() > CLIENTE_MAX) {
            motivo = "Nome do cliente com mais de " + CLIENTE_MAX + " caracteres";
        } else if (materialCodigo == null) {
            motivo = "Código do material vazio";
        } else if (quantidade == null || quantidade.signum() <= 0) {
            motivo = "Quantidade vazia ou não positiva";
        } else if (entrega == null) {
            motivo = "Data de entrega vazia ou inválida";
        }

        if (motivo != null) {
            rejeitadas.add(new LinhaRejeitada(numero, codigo != null ? codigo : bruto, motivo));
            return;
        }
        linhas.add(new LinhaOrdemImportada(numero, codigo, cliente, materialCodigo,
                material != null ? material : materialCodigo, quantidade, entrega, status));
    }

    private static Map<String, Integer> colunas(Row cabecalho, DataFormatter fmt) {
        Map<String, Integer> m = new HashMap<>();
        for (Cell c : cabecalho) {
            m.putIfAbsent(fmt.formatCellValue(c).trim(), c.getColumnIndex());
        }
        return m;
    }

    private static boolean vazia(Row row, DataFormatter fmt) {
        for (Cell c : row) {
            if (!fmt.formatCellValue(c).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String texto(Row row, int coluna, DataFormatter fmt) {
        Cell c = row.getCell(coluna);
        String v = c == null ? "" : fmt.formatCellValue(c).trim();
        return v.isEmpty() ? null : v;
    }

    /** Numérica no ERP; aceita também texto ("1.5" ou "1,5"). */
    private static BigDecimal numero(Cell c, DataFormatter fmt) {
        if (c == null) {
            return null;
        }
        if (c.getCellType() == CellType.NUMERIC) {
            // 100.0 → 100 (e não 1E+2, que sairia assim no JSON)
            BigDecimal v = BigDecimal.valueOf(c.getNumericCellValue()).stripTrailingZeros();
            return v.scale() < 0 ? v.setScale(0) : v;
        }
        try {
            return new BigDecimal(fmt.formatCellValue(c).trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** O ERP grava a data como número serial ("46356"); texto dd/MM/yyyy também é aceito. */
    private static LocalDate data(Cell c) {
        if (c == null) {
            return null;
        }
        if (c.getCellType() == CellType.NUMERIC) {
            return DateUtil.getLocalDateTime(c.getNumericCellValue()).toLocalDate();
        }
        if (c.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(c.getStringCellValue().trim(),
                        DD_MM_YYYY);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    private static String codigo(String bruto) {
        Matcher m = CODIGO_OP.matcher(bruto == null ? "" : bruto);
        return m.find() ? m.group().toUpperCase(Locale.ROOT) : null;
    }
}
