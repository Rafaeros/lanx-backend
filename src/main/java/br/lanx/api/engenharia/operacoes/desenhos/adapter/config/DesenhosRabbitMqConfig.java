package br.lanx.api.engenharia.operacoes.desenhos.adapter.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.lanx.api.shared.messaging.RabbitMQProperties;

/**
 * Filas dos desenhos no exchange {@code lanx.events}:
 *
 * <pre>
 * backend ──engenharia.desenho.solicitado──► lanx.engenharia.desenho-jobs ──► worker Python
 * worker  ──engenharia.desenho.resultado───► lanx.engenharia.desenho-resultados ──► DesenhoResultadoListener
 * </pre>
 *
 * A fila de jobs também é declarada aqui, com os MESMOS argumentos do worker (senão o RabbitMQ recusa com
 * PRECONDITION_FAILED): assim um pedido feito com o worker desligado fica guardado em vez de se perder.
 */
@Configuration
class DesenhosRabbitMqConfig {

    @Bean
    Queue desenhoResultadosQueue(RabbitMQProperties props) {
        return QueueBuilder.durable(props.queues().get("desenho-resultados")).build();
    }

    @Bean
    Binding desenhoResultadosBinding(Queue desenhoResultadosQueue, TopicExchange lanxExchange,
            RabbitMQProperties props) {
        return BindingBuilder.bind(desenhoResultadosQueue).to(lanxExchange)
                .with(props.routingKeys().get("desenho-resultado"));
    }

    @Bean
    Queue desenhoJobsDlq(RabbitMQProperties props) {
        return QueueBuilder.durable(props.queues().get("desenho-jobs") + ".dlq").build();
    }

    @Bean
    Queue desenhoJobsQueue(RabbitMQProperties props) {
        String fila = props.queues().get("desenho-jobs");
        return QueueBuilder.durable(fila)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", fila + ".dlq")
                .build();
    }

    @Bean
    Binding desenhoJobsBinding(Queue desenhoJobsQueue, TopicExchange lanxExchange, RabbitMQProperties props) {
        return BindingBuilder.bind(desenhoJobsQueue).to(lanxExchange)
                .with(props.routingKeys().get("desenho-solicitado"));
    }
}
