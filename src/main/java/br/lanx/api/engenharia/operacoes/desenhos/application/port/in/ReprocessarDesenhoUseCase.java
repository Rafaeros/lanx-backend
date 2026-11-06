package br.lanx.api.engenharia.operacoes.desenhos.application.port.in;

import java.util.UUID;

/** Gera de novo um job terminado (mesmo id, mesmos arquivos de entrada): volta para a fila. */
public interface ReprocessarDesenhoUseCase {

    void reprocessar(UUID jobId);
}
