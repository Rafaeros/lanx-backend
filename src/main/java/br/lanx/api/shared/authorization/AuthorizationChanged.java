package br.lanx.api.shared.authorization;

import java.util.UUID;

/**
 * Evento: "as permissões de alguém mudaram". Quem altera role ou usuário
 * publica;
 * o cache de authorities escuta. Assim os módulos não dependem uns dos outros
 * só para invalidar cache.
 */
public record AuthorizationChanged(UUID userId) {

    /** Mudou a role de todo mundo que a tem: descarta o cache inteiro. */
    public static AuthorizationChanged everyone() {
        return new AuthorizationChanged(null);
    }

    public static AuthorizationChanged ofUser(UUID userId) {
        return new AuthorizationChanged(userId);
    }

    public boolean affectsEveryone() {
        return userId == null;
    }
}
