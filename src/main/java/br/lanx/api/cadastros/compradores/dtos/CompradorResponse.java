package br.lanx.api.cadastros.compradores.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.compradores.Comprador;

public record CompradorResponse(
        UUID id,
        String nome,
        /** Null quando o comprador não está vinculado a um usuário do LANX. */
        UsuarioSummary usuario) {

    public record UsuarioSummary(UUID id, String username) {
    }

    public static CompradorResponse of(Comprador comprador, String username) {
        UUID userId = comprador.getUserId();
        return new CompradorResponse(comprador.getId(), comprador.getNome(),
                userId == null ? null : new UsuarioSummary(userId, username));
    }
}
