package br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte;

/**
 * Uma ponta de cabo presa a um conector, como está escrita na lista de corte: "CNFAT04CZ (C1)" na coluna
 * CONECT e "3" na coluna CAV.
 *
 * @param codigo   código do conector (ex.: {@code CNFAT04CZ})
 * @param ref      referência no chicote (ex.: {@code C1})
 * @param cavidade cavidade/via usada; pode vir vazia ou não numérica ("+", "XX")
 */
public record PontaConector(String codigo, String ref, String cavidade) {

    public PontaConector {
        codigo = codigo == null ? "" : codigo.trim().toUpperCase();
        ref = ref == null ? "" : ref.trim().toUpperCase();
        cavidade = cavidade == null ? "" : cavidade.trim().toUpperCase();
    }
}
