package br.lanx.api.engenharia.operacoes.desenhos.domain;

/**
 * Ciclo de vida do pedido de desenho.
 *
 * <pre>
 * PENDENTE ──► PROCESSANDO ──► CONCLUIDO
 *    │              └────────► ERRO
 *    └──► CONCLUIDO / ERRO       (o aviso PROCESSANDO pode se perder; o resultado final vale)
 * CONCLUIDO / ERRO ──► PENDENTE  (reprocessar)
 * </pre>
 */
public enum StatusDesenho {
    PENDENTE("Na fila"),
    PROCESSANDO("Processando"),
    CONCLUIDO("Concluído"),
    ERRO("Erro");

    private final String rotulo;

    StatusDesenho(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** O worker já terminou (com ou sem sucesso). */
    public boolean isFinal() {
        return this == CONCLUIDO || this == ERRO;
    }

    public boolean podeIrPara(StatusDesenho destino) {
        return switch (this) {
            case PENDENTE -> destino == PROCESSANDO || destino == CONCLUIDO || destino == ERRO;
            case PROCESSANDO -> destino == CONCLUIDO || destino == ERRO;
            case CONCLUIDO, ERRO -> destino == PENDENTE;
        };
    }
}
