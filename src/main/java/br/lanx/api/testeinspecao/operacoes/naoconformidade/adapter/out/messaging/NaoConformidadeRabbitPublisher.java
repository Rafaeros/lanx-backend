package br.lanx.api.testeinspecao.operacoes.naoconformidade.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import br.lanx.api.testeinspecao.operacoes.naoconformidade.events.NaoConformidadeCriadaEvent;
import br.lanx.api.shared.messaging.RabbitMQProperties;

@Component
public class NaoConformidadeRabbitPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;
    private static final Logger log = LoggerFactory.getLogger(NaoConformidadeRabbitPublisher.class);

    public NaoConformidadeRabbitPublisher(RabbitTemplate rabbitTemplate, RabbitMQProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onNaoConformidadeCriada(NaoConformidadeCriadaEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    props.exchange(), props.routingKeys().get("nao-conformidade-criada"), event);
        } catch (AmqpException e) {
            log.error("Falha ao publicar NaoConformidadeCriada {}", event.naoConformidadeId(), e);
        }
    }

}
