package br.lanx.api.cadastros.rateios.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.rateios.Rateio;

public record RateioResponse(UUID id, String nome) {

    public static RateioResponse fromEntity(Rateio rateio) {
        return new RateioResponse(rateio.getId(), rateio.getNome());
    }
}
