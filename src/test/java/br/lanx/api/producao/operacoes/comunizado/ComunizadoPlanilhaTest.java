package br.lanx.api.producao.operacoes.comunizado;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import br.lanx.api.producao.operacoes.comunizado.domain.CabecalhoComunizado;
import br.lanx.api.producao.operacoes.comunizado.domain.CoresCabos;
import br.lanx.api.producao.operacoes.comunizado.domain.ItemCorte;

class ComunizadoPlanilhaTest {

    private final ComunizadoPlanilha planilha = new ComunizadoPlanilha(CoresCabos.padrao());
    private final CabecalhoComunizado cabecalho = new CabecalhoComunizado("TESTE 000", null, LocalDate.of(2026, 10, 20),
            null, null, null, null, null);

    private static ItemCorte item(String circuito, String tipo) {
        return new ItemCorte(null, null, circuito, null, BigDecimal.valueOf(500), tipo, "TA", "1.0", "TB",
                "1.0");
    }

    /** Coluna TIPO (índice 4, sem gravação) da linha de dados {@code i}. */
    private static String fillXml(XSSFWorkbook wb, int i) {
        XSSFCellStyle estilo = wb.getSheetAt(0).getRow(10 + i).getCell(4).getCellStyle();
        return wb.getStylesSource().getFillAt((int) estilo.getCoreXf().getFillId()).getCTFill().xmlText();
    }

    @Test
    void caboBicolorTemGradienteNoTipo() throws Exception {
        byte[] bytes = planilha.gerar(cabecalho, List.of(item("C1", "CFR0035VDAM"), item("C2", "CFR0035VD")));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            String bicolor = fillXml(wb, 0);
            // verde (00B050) → amarelo (faixa FFC000), clareados 35%
            assertThat(bicolor).contains("gradientFill").contains("FF59CC8D").contains("FFFFD659");
            assertThat(fillXml(wb, 1)).doesNotContain("gradientFill");
        }
    }

    @Test
    void cadaBicolorTemOProprioGradiente() throws Exception {
        byte[] bytes = planilha.gerar(cabecalho, List.of(item("C1", "CFR0035PRLR"), item("C2", "CFR0035VMBR")));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(fillXml(wb, 0)).isNotEqualTo(fillXml(wb, 1));
        }
    }

    @Test
    void quebraPaginaACada22Itens() throws Exception {
        List<ItemCorte> itens = java.util.stream.IntStream.rangeClosed(1, 45)
                .mapToObj(n -> item("C" + n, "CFR0035PR")).toList();
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(planilha.gerar(cabecalho, itens)))) {
            assertThat(wb.getSheetAt(0).getRowBreaks()).containsExactly(31, 53);
            assertThat(wb.getSheetAt(0).getPrintSetup().getFitHeight()).isEqualTo((short) 3);
        }
    }
}
