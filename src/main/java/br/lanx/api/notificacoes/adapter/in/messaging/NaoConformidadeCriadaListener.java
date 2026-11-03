package br.lanx.api.notificacoes.adapter.in.messaging;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.lanx.api.notificacoes.NotificacaoService;
import br.lanx.api.notificacoes.NotificacaoSseRegistry;
import br.lanx.api.notificacoes.dtos.NotificacaoResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.events.NaoConformidadeCriadaEvent;

@Component
class NaoConformidadeCriadaListener {

    private static final Logger log = LoggerFactory.getLogger(NaoConformidadeCriadaListener.class);

    private final NotificacaoService notificacaoService;
    private final NotificacaoSseRegistry sse;

    NaoConformidadeCriadaListener(NotificacaoService notificacaoService, NotificacaoSseRegistry sse) {
        this.notificacaoService = notificacaoService;
        this.sse = sse;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.notificacoes-nao-conformidade}")
    void onNaoConformidadeCriada(NaoConformidadeCriadaEvent event) {
        List<NotificacaoResponse> criadas = notificacaoService.notificarNaoConformidadeCriada(event);
        criadas.forEach(n -> sse.send(n.userId(), n));
        log.info("NC {}: {} notificação(ões) criada(s)", event.naoConformidadeId(), criadas.size());
    }
}
