package br.lanx.api.auth.fakes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.lanx.api.auth.permission.application.port.out.PermissionCache;
import br.lanx.api.auth.permission.domain.PermissionSet;

public class InMemoryPermissionCache implements PermissionCache {

    private final Map<UUID, PermissionSet> entries = new HashMap<>();

    @Override
    public Optional<PermissionSet> get(UUID userId) {
        return Optional.ofNullable(entries.get(userId));
    }

    @Override
    public void put(UUID userId, PermissionSet permissions) {
        entries.put(userId, permissions);
    }

    @Override
    public void invalidate(UUID userId) {
        entries.remove(userId);
    }

    @Override
    public void invalidateAll() {
        entries.clear();
    }
}
