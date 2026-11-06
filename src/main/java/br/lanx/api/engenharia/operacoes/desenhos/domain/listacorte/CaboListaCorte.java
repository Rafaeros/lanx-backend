package br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte;

/**
 * Uma linha da aba de cabos. As pontas são {@code null} quando não vão a um conector (emenda/BONDER, ponta
 * sem referência).
 *
 * @param linha linha na planilha (1 = primeira), para o usuário achar o cabo
 */
public record CaboListaCorte(String circ, String aba, int linha, PontaConector a, PontaConector b) {
}
