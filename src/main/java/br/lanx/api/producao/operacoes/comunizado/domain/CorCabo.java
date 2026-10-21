package br.lanx.api.producao.operacoes.comunizado.domain;

/**
 * Uma cor de cabo do mapeamento (resources/producao/comunizado/cores-cabos.csv).
 *
 * @param codigo  sigla no código do cabo ("AZ", "AZCL")
 * @param texto   cor da fonte, hex RGB ("0070C0")
 * @param fundo   cor de fundo, hex RGB ou nulo
 * @param faixa   cor da sigla quando esta cor é a faixa de um bicolor (hex)
 * @param ordem   posição no arquivo: ordem das cores no comunizado
 */
public record CorCabo(String codigo, String nome, String texto, String fundo, boolean negrito, String faixa,
        int ordem) {
}
