package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.RegistrarResultadoDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJobArquivo;

@Service
class RegistrarResultadoDesenhoService implements RegistrarResultadoDesenhoUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarResultadoDesenhoService.class);

    private final DesenhoJobRepository jobs;
    private final Clock clock;

    RegistrarResultadoDesenhoService(DesenhoJobRepository jobs, Clock clock) {
        this.jobs = jobs;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void registrar(ResultadoDesenhoCommand cmd) {
        DesenhoJob job = jobs.findById(cmd.jobId()).orElse(null);
        if (job == null) {
            // job apagado ou de outro ambiente: lançar exceção só faria o Spring tentar de novo à toa
            log.warn("Resultado {} do desenho {} ignorado: job não existe", cmd.status(), cmd.jobId());
            return;
        }

        // as regras (e a idempotência das mensagens repetidas) estão no domínio
        switch (cmd.status()) {
            case PROCESSANDO -> job.iniciar(clock.instant());
            case CONCLUIDO -> job.concluir(
                    DesenhoJobArquivo.resultado(cmd.zip().chave(), cmd.zip().nome(), cmd.zip().tamanho(), clock),
                    cmd.pendencias(), clock.instant());
            case ERRO -> job.falhar(cmd.erro(), clock.instant());
            case PENDENTE -> {
                return; // o worker nunca manda PENDENTE
            }
        }
        jobs.save(job);
        log.info("Desenho {}: {}", job.getId(), job.getStatus());
    }
}
