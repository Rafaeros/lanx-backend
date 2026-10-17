package br.lanx.api.cadastros.fornecedores.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFornecedorRequest(
        @NotBlank @Size(max = 255) String nome,
        UUID paisId) {
}
