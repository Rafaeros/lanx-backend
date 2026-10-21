package br.lanx.api.producao.operacoes.comunizado;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Usa os exemplos reais de docs/: IT002 (entrada) no layout do ORN105 (saída). */
class ComunizadoServiceTest {

    private static final Path IT002 = Path.of("docs", "IT002_05 - KHN321 - 14.04.2026 (2).xlsx");
    /** IT sem CIRC (só GRAVAÇÃO) e com a lista de corte continuando em "IT CABOS (2)". */
    private static final Path MWM030 = Path.of("docs", "IT002_05 -MWM030- 12.08.2025 (1).xlsx");

    private final ComunizadoService service = new ComunizadoService();
    private final DataFormatter fmt = new DataFormatter();

    private static ComunizadoRequest request(String operador, String op, String quantidade) {
        return new ComunizadoRequest(null, null, LocalDate.of(2026, 10, 20), quantidade, op, null, null, operador);
    }

    private MockMultipartFile it002() throws Exception {
        return arquivo(IT002);
    }

    private static MockMultipartFile arquivo(Path path) throws Exception {
        return new MockMultipartFile("arquivo", path.getFileName().toString(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Files.readAllBytes(path));
    }

    @Test
    void leSoOsCircuitosEParaNoRodapeDaIt() throws Exception {
        var resultado = service.executar(it002(), request("JOAO", "OP-123", "74"));

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(resultado.conteudo()))) {
            Sheet sheet = wb.getSheetAt(0);
            // cabeçalho do ORN105: produto e data da revisão vêm da IT; o resto da requisição
            assertThat(texto(sheet, 0, 3)).isEqualTo("COMUNIZADO");
            assertThat(texto(sheet, 3, 1)).isEqualTo("KHN321 000 000");
            assertThat(sheet.getRow(1).getCell(6).getLocalDateTimeCellValue().toLocalDate())
                    .isEqualTo(LocalDate.of(2026, 4, 14));
            assertThat(texto(sheet, 5, 1)).isEqualTo("74 PÇS");
            assertThat(texto(sheet, 7, 1)).isEqualTo("OP-123");
            assertThat(linhaTexto(sheet.getRow(9))).containsExactly("PROD.", "OPERADOR", "CIRC", "COMP", "TIPO",
                    "TERMINAL A", "DESP. A", "TERMINAL B", "DESP. B");

            // só os 4 circuitos: nota e legenda do rodapé (linhas 32-35) ficam de fora
            List<String> circuitos = new ArrayList<>();
            for (int i = 10; i <= sheet.getLastRowNum(); i++) {
                circuitos.add(texto(sheet, i, 2));
            }
            // ordem: modelo, bitola, cor (ordem de cores-cabos.csv: AM, VD, AZ), maior comprimento, circuito
            assertThat(circuitos).containsExactly("ATH01", "ATH02", "ATH03", "ATH04");
            assertThat(linhaTexto(sheet.getRow(10))).startsWith("KHN321", "JOAO", "ATH01", "760", "CFR0100AM",
                    "TMFDTMNS00BT0715(TR02)", "3.5", "TMFDTNS00BT1025(TR02)", "4.0");
        }
        assertThat(resultado.nomeArquivo()).isEqualTo("KHN321 000 000 dia 20-10-2026.xlsx");
    }

    @Test
    void camposDaRequisicaoSobrescrevemAIt() throws Exception {
        var req = new ComunizadoRequest("ORN105 001 000", LocalDate.of(2025, 6, 17), LocalDate.of(2025, 9, 17),
                "2 CX", null, "08:00", "17:00", null);
        var resultado = service.executar(it002(), req);
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(resultado.conteudo()))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(texto(sheet, 3, 1)).isEqualTo("ORN105 001 000");
            assertThat(texto(sheet, 5, 1)).isEqualTo("2 CX");
            assertThat(texto(sheet, 3, 6)).isEqualTo("08:00");
            assertThat(texto(sheet, 10, 0)).isEqualTo("ORN105");
            assertThat(texto(sheet, 10, 1)).isEmpty(); // sem operador: em branco para preencher
        }
    }

    @Test
    void leGravacaoETodasAsAbasDeCabosEPaginaACada22() throws Exception {
        var resultado = service.executar(arquivo(MWM030), request(null, null, null));

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(resultado.conteudo()))) {
            Sheet sheet = wb.getSheetAt(0);
            // a IT tem GRAVAÇÃO: a coluna entra depois de CIRC
            assertThat(linhaTexto(sheet.getRow(9))).containsExactly("PROD.", "OPERADOR", "CIRC", "GRAVAÇÃO", "COMP",
                    "TIPO", "TERMINAL A", "DESP. A", "TERMINAL B", "DESP. B");
            assertThat(texto(sheet, 3, 1)).isEqualTo("MWM030 000 000");

            // 22 circuitos de "IT CABOS" + 7 de "IT CABOS (2)"; IT TUBOS fica de fora
            List<String> circuitos = new ArrayList<>();
            for (int i = 10; i <= sheet.getLastRowNum(); i++) {
                circuitos.add(texto(sheet, i, 2));
            }
            assertThat(circuitos).hasSize(29).contains("01", "22", "23", "29");

            // o circuito vem do fim da gravação, que sai inteira na coluna GRAVAÇÃO
            int linha01 = 10 + circuitos.indexOf("01");
            assertThat(texto(sheet, linha01, 3)).isEqualTo("31MP1-XP1:1 / 01");

            // 22 itens por página A4, com o cabeçalho do comunizado repetido
            assertThat(sheet.getRowBreaks()).containsExactly(10 + 21);
            assertThat(sheet.getRepeatingRows().formatAsString()).isEqualTo("1:10");
            assertThat(wb.getSheetAt(0).getPrintSetup().getFitHeight()).isEqualTo((short) 2);
        }
    }

    @Test
    void semGravacaoNaoTemAColuna() throws Exception {
        var resultado = service.executar(it002(), request(null, null, null));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(resultado.conteudo()))) {
            assertThat(linhaTexto(wb.getSheetAt(0).getRow(9))).doesNotContain("GRAVAÇÃO");
            assertThat(wb.getSheetAt(0).getRowBreaks()).isEmpty();
        }
    }

    @Test
    void circuitoDaGravacao() {
        assertThat(ComunizadoService.circuitoDaGravacao("31MP1-XP1:1 / 01")).isEqualTo("01");
        assertThat(ComunizadoService.circuitoDaGravacao("XP9:3-B6:1  / 26")).isEqualTo("26");
        assertThat(ComunizadoService.circuitoDaGravacao("CET17")).isEqualTo("CET17");
    }

    /** IT montada em memória: cabeçalho na linha 6, linhas de dados e, por fim, a nota do rodapé. */
    private static MockMultipartFile itSintetica(String colunaCircuito, List<Object[]> linhas) throws Exception {
        try (var wb = new XSSFWorkbook(); var out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("IT CABOS");
            sheet.createRow(0).createCell(7).setCellValue("Código");
            sheet.createRow(1).createCell(7).setCellValue("TESTE 000");
            String[] cab = { colunaCircuito, "COMP", "TIPO", "TERMINAL A", "DESP. A", "TERMINAL B", "DESP. B" };
            Row header = sheet.createRow(5);
            for (int c = 0; c < cab.length; c++) {
                header.createCell(c).setCellValue(cab[c]);
            }
            int r = 6;
            for (Object[] linha : linhas) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < linha.length; c++) {
                    if (linha[c] != null) {
                        row.createCell(c).setCellValue(String.valueOf(linha[c]));
                    }
                }
            }
            sheet.createRow(r + 3).createCell(0).setCellValue("NOTA 1: rodapé da IT");
            wb.write(out);
            return new MockMultipartFile("arquivo", "it.xlsx", null, out.toByteArray());
        }
    }

    @Test
    void aceitaCirNasItsAntigas() throws Exception {
        var it = itSintetica("CIR", List.<Object[]>of(new Object[] { "C01", "500", "CFR0050PR", "TA", "3.5", "TB", "4.0" }));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(service.executar(it, request(null, null, null)).conteudo()))) {
            assertThat(texto(wb.getSheetAt(0), 10, 2)).isEqualTo("C01");
        }
    }

    @Test
    void soORodapeEncerraATabelaLinhaIncompletaViraErro() throws Exception {
        // 30 circuitos (mais que 22): todos são lidos até a nota
        List<Object[]> linhas = new ArrayList<>();
        for (int n = 1; n <= 30; n++) {
            linhas.add(new Object[] { "C" + n, "500", "CFR0050PR", "TA", "3.5", "TB", "4.0" });
        }
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(
                service.executar(itSintetica("CIRC", linhas), request(null, null, null)).conteudo()))) {
            assertThat(wb.getSheetAt(0).getLastRowNum()).isEqualTo(10 + 29);
        }

        // linha sem COMP/TIPO antes do rodapé: não encerra a leitura em silêncio, avisa
        linhas.add(5, new Object[] { "C99", null, null, "TA", "3.5", "TB", "4.0" });
        assertThatThrownBy(() -> service.executar(itSintetica("CIRC", linhas), request(null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("COMP");
    }

    @Test
    void semArquivoDa400() {
        assertThatThrownBy(() -> service.executar(
                new MockMultipartFile("arquivo", "x.xlsx", null, new byte[0]), request(null, null, null)))
                .isInstanceOf(ResponseStatusException.class);
    }

    private String texto(Sheet sheet, int linha, int coluna) {
        Row row = sheet.getRow(linha);
        return row == null || row.getCell(coluna) == null ? "" : fmt.formatCellValue(row.getCell(coluna));
    }

    private List<String> linhaTexto(Row row) {
        List<String> valores = new ArrayList<>();
        for (int c = 0; c < row.getLastCellNum(); c++) {
            valores.add(row.getCell(c) == null ? "" : fmt.formatCellValue(row.getCell(c)));
        }
        return valores;
    }
}
