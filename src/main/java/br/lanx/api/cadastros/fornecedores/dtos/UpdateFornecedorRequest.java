package br.lanx.api.cadastros.fornecedores.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Substitui os dados do fornecedor: paisId nulo remove o país. */
public record UpdateFornecedorRequest(
        @NotBlank @Size(max = 255) String nome,
        UUID paisId) {
}
