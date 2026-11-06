package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.engenharia.operacoes.desenhos.application.event.DesenhoSolicitadoEvent;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.ReprocessarDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoJobNotFoundException;

@Service
class ReprocessarDesenhoService implements ReprocessarDesenhoUseCase {

    private final DesenhoJobRepository jobs;
    private final ApplicationEventPublisher eventos;

    ReprocessarDesenhoService(DesenhoJobRepository jobs, ApplicationEventPublisher eventos) {
        this.jobs = jobs;
        this.eventos = eventos;
    }

    @Override
    @Transactional
    public void reprocessar(UUID jobId) {
        DesenhoJob job = jobs.findById(jobId).orElseThrow(() -> new DesenhoJobNotFoundException(jobId));
        job.reprocessar(); // só a partir de CONCLUIDO ou ERRO: a regra está no domínio
        jobs.save(job);
        eventos.publishEvent(new DesenhoSolicitadoEvent(job)); // mesmo jobId: o worker sobrescreve o zip
    }
}
