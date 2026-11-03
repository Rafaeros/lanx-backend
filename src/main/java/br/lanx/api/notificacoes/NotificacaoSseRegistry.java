package br.lanx.api.notificacoes;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import br.lanx.api.notificacoes.dtos.NotificacaoResponse;

@Component
public class NotificacaoSseRegistry {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoSseRegistry.class);
    private static final long TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    private final Map<UUID, List<SseEmitter>> conexoes = new ConcurrentHashMap<>();

    public SseEmitter connect(UUID userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        conexoes.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable remove = () -> remove(userId, emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());
        send(userId, emitter, SseEmitter.event().name("conectado").data("ok"));
        return emitter;
    }

    public void send(UUID userId, NotificacaoResponse notificacao) {
        conexoes.getOrDefault(userId, List.of())
                .forEach(
                        emitter -> send(userId, emitter, SseEmitter.event()
                                .id(notificacao.id().toString())
                                .name("notificacao")
                                .data(notificacao)));
    }

    @Scheduled(fixedRate = 25_000)
    void heartbeat() {
        conexoes.forEach((userId, list) -> list.forEach(
                emitter -> send(userId, emitter, SseEmitter.event().comment("ping"))));
    }

    private void send(UUID userId, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | IllegalStateException e) {
            // cliente foi embora (aba fechada, reload, rede): não é erro do servidor
            log.debug("Conexão SSE do usuário {} encerrada: {}", userId, e.getMessage());
            remove(userId, emitter);
            emitter.complete();
        }
    }

    private void remove(UUID userId, SseEmitter emitter) {
        conexoes.computeIfPresent(userId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }
}
