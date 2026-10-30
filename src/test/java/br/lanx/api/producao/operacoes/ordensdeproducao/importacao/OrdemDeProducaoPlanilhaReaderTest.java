package br.lanx.api.producao.operacoes.ordensdeproducao.importacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.LinhaRejeitada;

/** Planilhas montadas em memória no layout da exportação de OPs do ERP. */
class OrdemDeProducaoPlanilhaReaderTest {

    /** Cabeçalho real, com "Código Insumo" repetido e a ordem do ERP. */
    static final List<String> CABECALHO = List.of("Criação", "Entrega", "Código", "Cliente", "Cód. Material",
            "Material", "Quantidade", "Quantidade Realizada", "Status", "Código Insumo", "Quantidade Insumo",
            "Código Insumo");

    @Test
    void leLinhaDoErpComDataSerialECodigoComAnotacao() throws IOException {
        var leitura = ler(CABECALHO,
                linha(LocalDate.of(2026, 11, 30), "OP-0007174   - usado op kanban 6268", "OXBO DO BRASIL (UBERABA)",
                        "OXB065 000 000", "BATTERY TO ISOLATOR", 2, "Iniciada"));

        assertThat(leitura.rejeitadas()).isEmpty();
        var l = leitura.linhas().get(0);
        assertThat(l.linha()).isEqualTo(2);
        assertThat(l.codigo()).isEqualTo("OP-0007174");
        assertThat(l.entrega()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(l.quantidade()).isEqualByComparingTo("2");
        assertThat(l.materialCodigo()).isEqualTo("OXB065 000 000");
        assertThat(l.finalizada()).isFalse();
    }

    @Test
    void rejeitaLinhaInvalidaSemDerrubarAsOutras() throws IOException {
        var leitura = ler(CABECALHO,
                linha(LocalDate.of(2026, 11, 30), "sem código", "Cliente", "MAT", "Mat", 1, "Liberada"),
                linha(null, "OP-0000002", "Cliente", "MAT", "Mat", 1, "Liberada"),
                linha(LocalDate.of(2026, 11, 30), "OP-0000003", "Cliente", "MAT", "Mat", 0, "Liberada"),
                linha(LocalDate.of(2026, 11, 30), "OP-0000004", "Cliente", "MAT", "Mat", 5, "Finalizada"));

        assertThat(leitura.rejeitadas()).extracting(LinhaRejeitada::linha).containsExactly(2, 3, 4);
        assertThat(leitura.rejeitadas().get(0).motivo()).contains("Código da OP inválido");
        assertThat(leitura.linhas()).singleElement().satisfies(l -> {
            assertThat(l.codigo()).isEqualTo("OP-0000004");
            assertThat(l.finalizada()).isTrue();
        });
    }

    @Test
    void colunaObrigatoriaAusenteRecusaOArquivo() {
        var semEntrega = CABECALHO.stream().filter(c -> !c.equals("Entrega")).toList();
        assertThatThrownBy(() -> ler(semEntrega))
                .isInstanceOf(PlanilhaOrdensInvalidasException.class)
                .hasMessageContaining("Entrega");
    }

    @Test
    void arquivoQueNaoEPlanilhaERecusado() {
        assertThatThrownBy(() -> OrdemDeProducaoPlanilhaReader.ler(new ByteArrayInputStream("texto".getBytes())))
                .isInstanceOf(PlanilhaOrdensInvalidasException.class);
    }

    // ---------- helpers ----------

    record Linha(LocalDate entrega, String codigo, String cliente, String materialCodigo, String material,
            double quantidade, String status) {
    }

    static Linha linha(LocalDate entrega, String codigo, String cliente, String materialCodigo, String material,
            double quantidade, String status) {
        return new Linha(entrega, codigo, cliente, materialCodigo, material, quantidade, status);
    }

    private static OrdemDeProducaoPlanilhaReader.Leitura ler(List<String> cabecalho, Linha... linhas)
            throws IOException {
        return OrdemDeProducaoPlanilhaReader.ler(new ByteArrayInputStream(planilha(cabecalho, linhas)));
    }

    /** .xlsx com o cabeçalho dado; cada linha preenche só as colunas que existem nele. */
    static byte[] planilha(List<String> cabecalho, Linha... linhas) throws IOException {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            XSSFSheet aba = wb.createSheet("Ordens");
            Row cab = aba.createRow(0);
            for (int i = 0; i < cabecalho.size(); i++) {
                cab.createCell(i).setCellValue(cabecalho.get(i));
            }
            for (int r = 0; r < linhas.length; r++) {
                Linha l = linhas[r];
                Row row = aba.createRow(r + 1);
                for (int i = 0; i < cabecalho.size(); i++) {
                    switch (cabecalho.get(i)) {
                        case "Entrega" -> {
                            if (l.entrega() != null) {
                                // como o ERP: número serial, não texto
                                row.createCell(i).setCellValue(DateUtil.getExcelDate(l.entrega()));
                            }
                        }
                        case "Código" -> row.createCell(i).setCellValue(l.codigo());
                        case "Cliente" -> row.createCell(i).setCellValue(l.cliente());
                        case "Cód. Material" -> row.createCell(i).setCellValue(l.materialCodigo());
                        case "Material" -> row.createCell(i).setCellValue(l.material());
                        case "Quantidade" -> row.createCell(i).setCellValue(l.quantidade());
                        case "Status" -> row.createCell(i).setCellValue(l.status());
                        default -> {
                        }
                    }
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }
}
