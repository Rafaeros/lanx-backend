package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import br.lanx.api.engenharia.operacoes.desenhos.application.event.DesenhoSolicitadoEvent;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJobArquivo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.FormatoDesenho;
import br.lanx.api.engenharia.operacoes.desenhos.domain.TipoArquivoDesenho;
import br.lanx.api.shared.messaging.RabbitMQProperties;

/** Manda o pedido para o worker depois que a transação que salvou o job fez commit. */
@Component
class DesenhoRabbitPublisher {

    private static final Logger log = LoggerFactory.getLogger(DesenhoRabbitPublisher.class);

    private final RabbitTemplate rabbit;
    private final RabbitMQProperties props;

    DesenhoRabbitPublisher(RabbitTemplate rabbit, RabbitMQProperties props) {
        this.rabbit = rabbit;
        this.props = props;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void aoSolicitar(DesenhoSolicitadoEvent evento) {
        DesenhoJob job = evento.job();
        try {
            rabbit.convertAndSend(props.exchange(), props.routingKeys().get("desenho-solicitado"), mensagem(job));
        } catch (AmqpException e) {
            // o job já está salvo como PENDENTE; com o RabbitMQ fora, ele não anda até alguém reenviar
            log.error("Falha ao publicar o desenho {} para o worker", job.getId(), e);
        }
    }

    static DesenhoSolicitadoMessage mensagem(DesenhoJob job) {
        var o = job.getOpcoes();
        return new DesenhoSolicitadoMessage(
                job.getId().toString(),
                job.getProjeto(),
                ref(job, TipoArquivoDesenho.LISTA_CORTE),
                ref(job, TipoArquivoDesenho.DESENHO_CLIENTE),
                new DesenhoSolicitadoMessage.Opcoes(
                        o.formatos().stream().map(FormatoDesenho::flag).sorted().toList(),
                        o.acabamento() == null ? null : o.acabamento().name(),
                        o.vistas(), o.biblioteca(), o.dwg()),
                job.getProjectOverride());
    }

    private static DesenhoSolicitadoMessage.ArquivoRef ref(DesenhoJob job, TipoArquivoDesenho tipo) {
        return job.arquivo(tipo)
                .map((DesenhoJobArquivo a) -> new DesenhoSolicitadoMessage.ArquivoRef(a.chaveArmazenamento(),
                        a.nomeArquivo()))
                .orElse(null);
    }
}
