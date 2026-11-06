package br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.ProblemaListaCorte.Nivel;

class AnaliseConectoresTest {

    private static CaboListaCorte cabo(String circ, String codA, String refA, String cavA, String codB,
            String refB, String cavB) {
        return new CaboListaCorte(circ, "IT CABOS", 7, new PontaConector(codA, refA, cavA),
                codB == null ? null : new PontaConector(codB, refB, cavB));
    }

    @ParameterizedTest
    @CsvSource({ "CNFAT04CZ,4", "CNMAT06CZ,6", "CNFDT06BG,6", "CNFDtE06PRCE,6", "CNMAT12CZL012,12",
            "CNMPFDH02PR,2" })
    void le_as_vias_do_codigo(String codigo, int vias) {
        assertThat(CodigoConector.vias(codigo)).hasValue(vias);
    }

    @ParameterizedTest
    @CsvSource({ "DD1KV3A", "RS18K1/4W", "PONTA LIVRE", "CNFAT4CZ", "CNF04CZ", "CNXCGNW101010", "CNFAT00CZ" })
    void codigo_fora_do_padrao_nao_tem_vias(String codigo) {
        assertThat(CodigoConector.vias(codigo)).isEmpty();
    }

    @Test
    void lista_correta_nao_tem_problema() {
        var cabos = List.of(
                cabo("W01", "CNFAT04CZ", "C1", "1", "CNMAT06CZ", "C2", "6"),
                cabo("W02", "CNFAT04CZ", "C1", "04", "CNMAT06CZ", "C2", "1"),
                cabo("W03", "CNFAT04CZ", "C1", "+", null, null, null));

        assertThat(AnaliseConectores.analisar(cabos)).isEmpty();
    }

    @Test
    void cavidade_alem_das_vias_e_erro() {
        var cabos = List.of(cabo("W01", "CNFAT04CZ", "C1", "5", "CNMAT06CZ", "C2", "1"));

        assertThat(AnaliseConectores.analisar(cabos)).singleElement().satisfies(p -> {
            assertThat(p.nivel()).isEqualTo(Nivel.ERRO);
            assertThat(p.conector()).isEqualTo("C1");
            assertThat(p.mensagem()).contains("4 vias", "cavidade 5", "W01");
        });
    }

    @Test
    void cavidade_duplicada_e_erro_mesmo_escrita_diferente() {
        var cabos = List.of(
                cabo("W01", "CNFAT04CZ", "C1", "2", "CNMAT06CZ", "C2", "1"),
                cabo("W02", "CNFAT04CZ", "C1", "02", "CNMAT06CZ", "C2", "2"));

        assertThat(AnaliseConectores.analisar(cabos)).singleElement().satisfies(p -> {
            assertThat(p.nivel()).isEqualTo(Nivel.ERRO);
            assertThat(p.mensagem()).contains("cavidade 2 duplicada", "W01", "W02");
        });
    }

    @Test
    void conector_fora_do_padrao_nao_e_conferido() {
        var cabos = List.of(
                cabo("W01", "DD1KV3A", "D1", "1", "RS18K1/4W", "R1", "1"),
                cabo("W02", "DD1KV3A", "D1", "1", "RS18K1/4W", "R1", "9"));

        assertThat(AnaliseConectores.analisar(cabos)).isEmpty();
    }

    @Test
    void mesma_ref_com_codigos_diferentes_e_so_aviso() {
        var cabos = List.of(
                cabo("W01", "CNFAT04CZ", "C1", "1", null, null, null),
                cabo("W02", "CNFAT06CZ", "C1", "2", null, null, null));

        assertThat(AnaliseConectores.analisar(cabos)).singleElement()
                .satisfies(p -> assertThat(p.nivel()).isEqualTo(Nivel.AVISO));
    }
}
