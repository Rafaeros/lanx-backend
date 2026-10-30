package br.lanx.api.shared.messaging;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record RabbitMQProperties(
        String exchange,
        Map<String, String> queues,
        Map<String, String> routingKeys) {
}
