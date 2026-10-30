package br.lanx.api.testeinspecao.operacoes.naoconformidade.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record NaoConformidadeCriadaEvent(
        UUID naoConformidadeId,
        UUID departamentoId,
        String tipo,
        LocalDate dataNaoConformidade,
        Instant criadoEm) {
}
