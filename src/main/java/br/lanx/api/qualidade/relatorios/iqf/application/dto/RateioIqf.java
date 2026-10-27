package br.lanx.api.qualidade.relatorios.iqf.application.dto;

import java.util.UUID;

/** Rateio (cadastros.rateios) usado no filtro do IQF. */
public record RateioIqf(UUID id, String nome) {
}
