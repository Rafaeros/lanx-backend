package br.lanx.api.notificacoes.dtos;

import java.time.Instant;
import java.util.UUID;

import br.lanx.api.notificacoes.Notificacao;
import br.lanx.api.notificacoes.TipoNotificacao;

public record NotificacaoResponse(
                UUID id, UUID userId, TipoNotificacao tipo, String titulo,
                String mensagem, String link, boolean lida, Instant createdAt) {

        public static NotificacaoResponse fromEntity(Notificacao n) {
                return new NotificacaoResponse(n.getId(), n.getUserId(), n.getTipo(), n.getTitulo(), n.getMensagem(),
                                n.getLink(), n.getLidaEm() != null, n.getCreatedAt());
        }
}
