package br.lanx.api.auth.permission.adapter.out.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import br.lanx.api.auth.permission.application.port.out.PermissionCache;
import br.lanx.api.auth.permission.domain.PermissionSet;

/**
 * Cache em memória (a aplicação roda numa instância só). A expiração é uma rede
 * de segurança: o caminho normal é invalidar quando role ou usuário muda.
 * Com mais de uma instância, troque este adapter por Redis; o port não muda.
 */
@Component
class InMemoryPermissionCache implements PermissionCache {

    private static final Duration TTL = Duration.ofMinutes(5);

    private record Entry(PermissionSet permissions, Instant expiresAt) {
    }

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    @Override
    public Optional<PermissionSet> get(UUID userId) {
        Entry entry = entries.get(userId);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            entries.remove(userId, entry);
            return Optional.empty();
        }
        return Optional.of(entry.permissions());
    }

    @Override
    public void put(UUID userId, PermissionSet permissions) {
        entries.put(userId, new Entry(permissions, Instant.now().plus(TTL)));
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
