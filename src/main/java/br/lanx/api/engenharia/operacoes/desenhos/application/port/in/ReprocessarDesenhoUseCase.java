package br.lanx.api.engenharia.operacoes.desenhos.application.port.in;

import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoOpcoes;

/** Gera de novo um job terminado (mesmo id, mesmos arquivos de entrada): volta para a fila. */
public interface ReprocessarDesenhoUseCase {

    /** @param opcoes as marcadas na tela do desenho; {@code null} mantém as do pedido */
    void reprocessar(UUID jobId, DesenhoOpcoes opcoes);

    default void reprocessar(UUID jobId) {
        reprocessar(jobId, null);
    }
}
