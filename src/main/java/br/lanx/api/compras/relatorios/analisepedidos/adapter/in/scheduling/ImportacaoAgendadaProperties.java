package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.scheduling;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.compras.analse-pedidos.importacao-agendada")
public record ImportacaoAgendadaProperties(
        @DefaultValue("-") String cron,
        @DefaultValue("180d") Duration janela) {
}
