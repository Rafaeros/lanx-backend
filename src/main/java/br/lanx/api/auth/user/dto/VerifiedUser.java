package br.lanx.api.auth.user.dto;

import java.util.UUID;

/** Resultado de uma credencial válida: só o que o módulo authentication precisa para emitir o token. */
public record VerifiedUser(UUID id, String username) {
}
