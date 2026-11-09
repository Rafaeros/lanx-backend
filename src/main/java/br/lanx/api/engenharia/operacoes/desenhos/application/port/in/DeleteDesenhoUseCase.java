package br.lanx.api.engenharia.operacoes.desenhos.application.port.in;

import java.util.UUID;

public interface DeleteDesenhoUseCase {
    void deleteById(UUID id);
}
