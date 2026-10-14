package br.lanx.api.auth.authorization;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import br.lanx.api.shared.authorization.AuthorizationChanged;

/**
 * Guarda as authorities por usuário, para não consultar o banco a cada
 * requisição. O caminho normal de expirar é o evento
 * {@link AuthorizationChanged}
 * (disparado após o commit de quem alterou role ou usuário); o TTL é rede de
 * segurança. Com mais de uma instância da API, troque por Redis.
 */
@Component
class AuthorityCache {

    private static final Duration TTL = Duration.ofMinutes(5);

    private record Entry(Set<String> authorities, Instant expiresAt) {
    }

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    Optional<Set<String>> get(UUID userId) {
        Entry entry = entries.get(userId);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            entries.remove(userId, entry);
            return Optional.empty();
        }
        return Optional.of(entry.authorities());
    }

    void put(UUID userId, Set<String> authorities) {
        entries.put(userId, new Entry(Set.copyOf(authorities), Instant.now().plus(TTL)));
    }

    // fallbackExecution: se quem publicou não estava numa transação, invalida na
    // hora
    @TransactionalEventListener(fallbackExecution = true)
    void onChange(AuthorizationChanged event) {
        if (event.affectsEveryone()) {
            entries.clear();
        } else {
            entries.remove(event.userId());
        }
    }
}
