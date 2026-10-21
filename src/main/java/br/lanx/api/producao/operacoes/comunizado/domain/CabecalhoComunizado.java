package br.lanx.api.producao.operacoes.comunizado.domain;

import java.time.LocalDate;

/**
 * Bloco de cabeçalho do comunizado (layout ORN105): parte vem da instrução de
 * trabalho (IT), parte é informada na requisição. Campos nulos saem em branco
 * para preenchimento à mão.
 *
 * @param produto       código do produto ("KHN321 000 000")
 * @param docReferencia data da revisão da IT usada
 * @param realizadoEm   data em que o comunizado foi feito
 * @param quantidade    ex.: "74 PÇS"
 * @param op            ordem de produção
 * @param inicio        início da produção (texto livre: data/hora)
 * @param fim           fim da produção
 * @param operador      operador, repetido na coluna OPERADOR de cada linha
 */
public record CabecalhoComunizado(
        String produto,
        LocalDate docReferencia,
        LocalDate realizadoEm,
        String quantidade,
        String op,
        String inicio,
        String fim,
        String operador) {

    /** "KHN321 000 000" → "KHN321" (coluna PROD. de cada linha). */
    public String produtoCurto() {
        if (produto == null || produto.isBlank()) {
            return "";
        }
        return produto.trim().split("\\s+")[0];
    }
}
