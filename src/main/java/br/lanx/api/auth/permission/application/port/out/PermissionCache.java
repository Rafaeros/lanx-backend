package br.lanx.api.auth.permission.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.auth.permission.domain.PermissionSet;

/**
 * Guarda as permissões efetivas por usuário, para não consultar o banco a cada
 * requisição. Quem altera role ou usuário deve chamar invalidate/invalidateAll.
 */
public interface PermissionCache {
    Optional<PermissionSet> get(UUID userId);

    void put(UUID userId, PermissionSet permissions);

    void invalidate(UUID userId);

    void invalidateAll();
}
