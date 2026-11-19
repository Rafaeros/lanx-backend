package br.lanx.api.cadastros.enderecos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @NotNull Integer cidadeId, // codigo_ibge
        @NotBlank @Size(max = 9) String cep,
        @NotBlank @Size(max = 150) String logradouro,
        @NotBlank @Size(max = 10) String numero,
        @Size(max = 100) String complemento,
        @NotBlank @Size(max = 100) String bairro) {
}
