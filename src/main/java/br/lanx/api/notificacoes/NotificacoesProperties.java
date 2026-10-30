package br.lanx.api.notificacoes;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notificacoes")
public record NotificacoesProperties(Destinatarios naoConformidadeCriada) {
    public record Destinatarios(List<String> departamentos) {
    }
}
