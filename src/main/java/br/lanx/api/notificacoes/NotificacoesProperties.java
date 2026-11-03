package br.lanx.api.notificacoes;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.notificacoes")
public record NotificacoesProperties(@NotNull @Valid Destinatarios naoConformidadeCriada) {
    public record Destinatarios(@NotEmpty List<String> departamentos) {
    }
}
