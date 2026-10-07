package br.lanx.api.shared.application;

import java.util.UUID;

/** Resultado padrão de um caso de uso que cria algo: o id do que foi criado. */
public record CreatedResult(UUID id) {
}
