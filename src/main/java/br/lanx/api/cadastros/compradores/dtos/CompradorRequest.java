package br.lanx.api.cadastros.compradores.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de POST e PUT em /compradores. {@code userId} nulo deixa (ou volta) o comprador sem usuário. */
public record CompradorRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 150) String nome,
        UUID userId) {
}
