package br.lanx.api.engenharia.operacoes.desenhos.application.dto;

import java.time.Instant;
import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;

public record DesenhoResumo(
        UUID id,
        String projeto,
        StatusDesenho status,
        int tentativas,
        int qtdPendencias,
        boolean temZip,
        Instant createdAt,
        Instant completedAt) {
}
