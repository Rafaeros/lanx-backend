package br.lanx.api.engenharia.operacoes.desenhos.adapter.in.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.RegistrarResultadoDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.RegistrarResultadoDesenhoUseCase.ResultadoDesenhoCommand;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.RegistrarResultadoDesenhoUseCase.ZipGerado;
import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;

/**
 * Recebe os avisos do worker. Exceção aqui = o Spring tenta de novo (retry do application.yaml) e depois
 * descarta; por isso mensagem inválida é só registrada no log, sem exceção.
 */
@Component
class DesenhoResultadoListener {

    private static final Logger log = LoggerFactory.getLogger(DesenhoResultadoListener.class);

    private final RegistrarResultadoDesenhoUseCase registrar;

    DesenhoResultadoListener(RegistrarResultadoDesenhoUseCase registrar) {
        this.registrar = registrar;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.desenho-resultados}")
    void aoReceber(DesenhoResultadoMessage msg) {
        ResultadoDesenhoCommand cmd;
        try {
            cmd = comando(msg);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Resultado de desenho inválido descartado: {} ({})", msg, e.getMessage());
            return;
        }
        registrar.registrar(cmd);
    }

    private static ResultadoDesenhoCommand comando(DesenhoResultadoMessage msg) {
        StatusDesenho status = StatusDesenho.valueOf(msg.status());
        ZipGerado zip = msg.zip() == null ? null
                : new ZipGerado(msg.zip().chave(), msg.zip().nomeOriginal(), msg.zip().tamanho());
        if (status == StatusDesenho.CONCLUIDO && zip == null)
            throw new IllegalArgumentException("CONCLUIDO sem zip");
        return new ResultadoDesenhoCommand(UUID.fromString(msg.jobId()), status, zip, msg.pendencias(), msg.erro());
    }
}
