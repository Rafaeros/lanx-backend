package br.lanx.api.cadastros.rateios.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de POST e PUT em /rateios. */
public record RateioRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 150) String nome) {
}
