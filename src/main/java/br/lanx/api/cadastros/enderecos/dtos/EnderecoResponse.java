package br.lanx.api.cadastros.enderecos.dtos;

import java.util.UUID;

public record EnderecoResponse(
        UUID enderecoId,
        Integer cidadeId, // codigo_ibge
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro) {
}
