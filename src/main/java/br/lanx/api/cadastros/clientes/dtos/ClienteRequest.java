package br.lanx.api.cadastros.clientes.dtos;

import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de POST e PUT em /clientes. CNPJ é opcional, com ou sem máscara (é salvo só com os dígitos). */
public record ClienteRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 250) String nome,
        @CNPJ(message = "CNPJ inválido") String cnpj) {
}
