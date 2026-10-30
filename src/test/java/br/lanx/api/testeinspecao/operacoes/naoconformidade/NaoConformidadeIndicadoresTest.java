package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeIndicadoresResponse.Grupo;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeResponse.OrdemDeProducaoResumo;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeResponse.Resumo;

class NaoConformidadeIndicadoresTest {

    private static final OrdemDeProducaoResumo OP_WITTUR = op("OP-0223707", "WITTUR", 800);
    private static final OrdemDeProducaoResumo OP_OXBO = op("OP-0223360", "OXBO", 200);

    @Test
    void totaisTaxaSobreOpsDistintasEGruposDoMaiorParaOMenor() {
        var ncs = List.of(
                nc(OP_WITTUR, "Inversão", "Montagem", 4, "2025-01-04"),
                nc(OP_WITTUR, "Conector errado", "Montagem", 10, "2025-01-04"),
                nc(OP_OXBO, "Inversão", "Corte", 6, "2025-03-07"));

        var r = NaoConformidadeIndicadores.calcular(ncs, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-03-31"));

        assertThat(r.registros()).isEqualTo(3);
        assertThat(r.quantidadeNaoConforme()).isEqualByComparingTo("20");
        assertThat(r.opsAfetadas()).isEqualTo(2);
        assertThat(r.quantidadeOps()).as("OP repetida conta uma vez").isEqualByComparingTo("1000");
        assertThat(r.percentualNaoConforme()).isEqualByComparingTo("2.00");

        assertThat(r.porDepartamento()).extracting(Grupo::nome).containsExactly("Montagem", "Corte");
        assertThat(r.porDepartamento().get(0).quantidade()).isEqualByComparingTo("14");
        assertThat(r.porDepartamento().get(0).percentual()).isEqualByComparingTo("70.00");
        assertThat(r.porDepartamento().get(1).percentualAcumulado()).isEqualByComparingTo("100.00");

        assertThat(r.porTipo()).extracting(Grupo::nome).containsExactly("Conector errado", "Inversão");
        assertThat(r.porTipo().get(1).registros()).isEqualTo(2);
        assertThat(r.porCliente()).extracting(Grupo::nome).containsExactly("WITTUR", "OXBO");
    }

    @Test
    void mesesDoPeriodoInteiroInclusiveSemRegistro() {
        var ncs = List.of(nc(OP_OXBO, "Inversão", "Corte", 6, "2025-03-07"));

        var r = NaoConformidadeIndicadores.calcular(ncs, LocalDate.parse("2025-01-01"), LocalDate.parse("2025-03-31"));

        assertThat(r.meses()).extracting(m -> m.mes()).containsExactly("2025-01", "2025-02", "2025-03");
        assertThat(r.meses().get(0).quantidade()).isEqualByComparingTo("0");
        assertThat(r.meses().get(2).registros()).isEqualTo(1);
    }

    @Test
    void semRegistrosNaoDividePorZero() {
        var r = NaoConformidadeIndicadores.calcular(List.of(), null, null);

        assertThat(r.quantidadeNaoConforme()).isEqualByComparingTo("0");
        assertThat(r.percentualNaoConforme()).isNull();
        assertThat(r.porDepartamento()).isEmpty();
        assertThat(r.meses()).isEmpty();
    }

    @Test
    void cadastroExcluidoViraGrupoProprio() {
        var semDepartamento = new NaoConformidadeResponse(UUID.randomUUID(), new Resumo(UUID.randomUUID(), "Inversão"),
                null, null, null, BigDecimal.ONE, LocalDate.parse("2025-01-04"), Instant.now(), null);

        var r = NaoConformidadeIndicadores.calcular(List.of(semDepartamento), null, null);

        assertThat(r.porDepartamento()).extracting(Grupo::nome).containsExactly(NaoConformidadeIndicadores.SEM_CADASTRO);
        assertThat(r.opsAfetadas()).isZero();
        assertThat(r.percentualNaoConforme()).as("sem OP, sem base para a taxa").isNull();
    }

    private static OrdemDeProducaoResumo op(String codigo, String cliente, int quantidade) {
        return new OrdemDeProducaoResumo(UUID.randomUUID(), codigo, cliente, "MAT", "Material",
                BigDecimal.valueOf(quantidade), LocalDate.parse("2025-01-24"));
    }

    private static NaoConformidadeResponse nc(OrdemDeProducaoResumo op, String tipo, String departamento,
            int quantidade, String data) {
        return new NaoConformidadeResponse(UUID.randomUUID(), new Resumo(UUID.randomUUID(), tipo),
                new Resumo(UUID.nameUUIDFromBytes(departamento.getBytes()), departamento), op, null,
                BigDecimal.valueOf(quantidade), LocalDate.parse(data), Instant.now(), null);
    }
}
