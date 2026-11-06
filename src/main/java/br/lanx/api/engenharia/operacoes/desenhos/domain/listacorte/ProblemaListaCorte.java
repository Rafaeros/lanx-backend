package br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte;

/**
 * Algo errado (ou suspeito) na lista de corte, encontrado antes de mandar gerar o desenho.
 *
 * @param conector referência do conector (ex.: {@code C1})
 */
public record ProblemaListaCorte(Nivel nivel, String conector, String mensagem) {

    /** {@code ERRO}: a lista está errada; {@code AVISO}: pode ser intencional, mas vale conferir. */
    public enum Nivel {
        ERRO, AVISO
    }
}
