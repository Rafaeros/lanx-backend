package br.lanx.api.cadastros.clientes.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.clientes.Cliente;

public record ClienteResponse(
        UUID id,
        String nome,
        String cnpj) {
    public static ClienteResponse fromEntity(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getCnpj());
    }
}
