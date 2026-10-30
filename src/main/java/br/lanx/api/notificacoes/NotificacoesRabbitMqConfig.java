package br.lanx.api.notificacoes;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.lanx.api.shared.messaging.RabbitMQProperties;

@Configuration
public class NotificacoesRabbitMqConfig {

    @Bean
    Queue notificacoesNaoConformidadeQueue(RabbitMQProperties props) {
        return QueueBuilder.durable(props.queues().get("notificacoes-nao-conformidade")).build();
    }

    @Bean
    Binding notificacoesNaoCOnformidadeBinding(Queue notificacoesNaoConformidadeQueue, TopicExchange lanxExchange,
            RabbitMQProperties props) {
        return BindingBuilder.bind(notificacoesNaoConformidadeQueue).to(lanxExchange)
                .with(props.routingKeys().get("nao-conformidade-criada"));
    }

}
