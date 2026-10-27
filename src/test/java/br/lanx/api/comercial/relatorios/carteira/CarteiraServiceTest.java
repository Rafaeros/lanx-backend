package br.lanx.api.comercial.relatorios.carteira;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.lanx.api.comercial.relatorios.carteira.dtos.CarteiraResponse;
import br.lanx.api.comercial.relatorios.carteira.erp.CarteiraErpClient;
import br.lanx.api.comercial.relatorios.carteira.erp.CarteiraLinhaErp;
import br.lanx.api.comercial.relatorios.carteira.erp.CarteiraLinhaErp.MaterialPendenteErp;
import br.lanx.api.comercial.relatorios.carteira.exceptions.PeriodoCarteiraInvalidoException;

class CarteiraServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 31);

    /** ERP falso: conta as consultas e devolve sempre as mesmas linhas. */
    private static final class ErpFalso implements CarteiraErpClient {
        int consultas;
        List<CarteiraLinhaErp> linhas = List.of();

        @Override
        public List<CarteiraLinhaErp> buscar(LocalDate dataInicio, LocalDate dataFim) {
            consultas++;
            return linhas;
        }
    }

    /** Relógio que dá para avançar: hoje = 27/10/2026. */
    private static final class Relogio extends Clock {
        Instant agora = Instant.parse("2026-10-27T12:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }

    private final ErpFalso erp = new ErpFalso();
    private final Relogio relogio = new Relogio();
    private final CarteiraService service = new CarteiraService(erp, relogio);

    private static CarteiraLinhaErp linha(int negociacao, LocalDate previsao, String valor,
            MaterialPendenteErp... materiais) {
        return new CarteiraLinhaErp(negociacao, null, "OP-" + negociacao, "Venda", "Cliente", "COD", "Produto",
                previsao, BigDecimal.ONE, valor == null ? null : new BigDecimal(valor), "CORTE", List.of(materiais));
    }

    @Test
    void ordenaPelaPrevisaoMarcaAtrasadosEResume() {
        var material = new MaterialPendenteErp("MAT", "Conector", BigDecimal.ONE, "un", null);
        erp.linhas = List.of(
                linha(3, null, "10.00"),
                linha(2, LocalDate.of(2026, 11, 5), "100.50", material),
                linha(1, LocalDate.of(2026, 10, 20), null));

        CarteiraResponse carteira = service.consultar(INICIO, FIM, false);

        assertThat(carteira.itens()).extracting(CarteiraResponse.Item::negociacao).containsExactly(1, 2, 3);
        assertThat(carteira.itens()).extracting(CarteiraResponse.Item::atrasado).containsExactly(true, false, false);
        assertThat(carteira.resumo().itens()).isEqualTo(3);
        assertThat(carteira.resumo().valorTotal()).isEqualByComparingTo("110.50");
        assertThat(carteira.resumo().atrasados()).isEqualTo(1);
        assertThat(carteira.resumo().comMaterialPendente()).isEqualTo(1);
        assertThat(carteira.itens().get(1).materiaisPendentes()).singleElement()
                .extracting(CarteiraResponse.MaterialPendente::codigo).isEqualTo("MAT");
    }

    @Test
    void reaproveitaAConsultaRecenteDoMesmoPeriodo() {
        Instant primeira = service.consultar(INICIO, FIM, false).consultadoEm();
        relogio.agora = relogio.agora.plusSeconds(5 * 60);

        assertThat(service.consultar(INICIO, FIM, false).consultadoEm()).isEqualTo(primeira);
        assertThat(erp.consultas).isEqualTo(1);

        service.consultar(INICIO, LocalDate.of(2026, 6, 30), false);
        assertThat(erp.consultas).as("outro período consulta o ERP").isEqualTo(2);
    }

    @Test
    void atualizarOuCacheVencidoConsultamOErpDeNovo() {
        service.consultar(INICIO, FIM, false);
        service.consultar(INICIO, FIM, true);
        assertThat(erp.consultas).isEqualTo(2);

        relogio.agora = relogio.agora.plus(CarteiraService.VALIDADE).plusSeconds(1);
        assertThat(service.consultar(INICIO, FIM, false).consultadoEm()).isEqualTo(relogio.agora);
        assertThat(erp.consultas).isEqualTo(3);
    }

    @Test
    void periodoInvertidoNaoConsultaOErp() {
        assertThatThrownBy(() -> service.consultar(FIM, INICIO, false))
                .isInstanceOf(PeriodoCarteiraInvalidoException.class);
        assertThat(erp.consultas).isZero();
    }
}
