package br.lanx.api.comercial.relatorios.carteira;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import br.lanx.api.comercial.relatorios.carteira.dtos.CarteiraResponse;

class CarteiraPlanilhaTest {

    private static final LocalDate PREVISAO = LocalDate.of(2026, 11, 27);

    private static CarteiraResponse.Item item(int negociacao, boolean atrasado,
            CarteiraResponse.MaterialPendente... materiais) {
        return new CarteiraResponse.Item(negociacao, "PC-" + negociacao, "OP-" + negociacao, "Venda", "Cliente",
                "COD", "Produto", PREVISAO, BigDecimal.ONE, new BigDecimal("100.00"), "CORTE", atrasado,
                List.of(materiais));
    }

    private static CarteiraResponse.MaterialPendente material(String codigo, LocalDate previsao) {
        return new CarteiraResponse.MaterialPendente(codigo, "Conector", BigDecimal.TWO, "un", previsao);
    }

    @Test
    void carteiraComTotalEUmaLinhaPorMaterial() throws Exception {
        var carteira = new CarteiraResponse(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                Instant.parse("2026-10-27T12:00:00Z"), new CarteiraResponse.Resumo(2, new BigDecimal("200"), 1, 1),
                List.of(item(1, true), item(2, false, material("M1", null), material("M2", null))));

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(CarteiraPlanilha.gerar(carteira)))) {
            var aba = wb.getSheet("Carteira");
            assertThat(aba.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Carteira de pedidos");
            assertThat(aba.getRow(3).getCell(0).getStringCellValue()).isEqualTo("Negociação");
            assertThat(aba.getRow(4).getCell(2).getStringCellValue()).isEqualTo("OP-1");
            assertThat(aba.getRow(4).getCell(12).getStringCellValue()).isEqualTo("Atrasado");
            assertThat(aba.getRow(5).getCell(11).getNumericCellValue()).isEqualTo(2);
            assertThat(aba.getRow(6).getCell(9).getCellFormula()).isEqualTo("SUBTOTAL(109,J5:J6)");
            assertThat(aba.getDrawingPatriarch().getShapes()).as("logo").hasSize(1);

            var materiais = wb.getSheet("Materiais pendentes");
            assertThat(materiais.getRow(4).getCell(6).getStringCellValue()).isEqualTo("M1");
            assertThat(materiais.getRow(5).getCell(6).getStringCellValue()).isEqualTo("M2");
            assertThat(materiais.getRow(5).getCell(1).getStringCellValue()).isEqualTo("OP-2");
            assertThat(materiais.getLastRowNum()).isEqualTo(5);
        }
    }

    @Test
    void materialEmRiscoSeChegaDepoisDoPedidoOuJaVenceu() {
        var pedido = item(1, false);
        var hoje = LocalDate.of(2026, 10, 27);
        assertThat(CarteiraPlanilha.materialEmRisco(material("M", PREVISAO.plusDays(1)), pedido, hoje)).isTrue();
        assertThat(CarteiraPlanilha.materialEmRisco(material("M", hoje.minusDays(1)), pedido, hoje)).isTrue();
        assertThat(CarteiraPlanilha.materialEmRisco(material("M", hoje.plusDays(3)), pedido, hoje)).isFalse();
        assertThat(CarteiraPlanilha.materialEmRisco(material("M", null), pedido, hoje)).isFalse();
    }
}
