package br.lanx.api.shared.security;

import java.util.UUID;

/**
 * Quem está chamando. Só identidade: as permissões NÃO viajam no token, são
 * resolvidas no servidor (com cache), para que mudanças valham na hora.
 */
public record AuthenticatedUser(UUID id, String username) {
}
