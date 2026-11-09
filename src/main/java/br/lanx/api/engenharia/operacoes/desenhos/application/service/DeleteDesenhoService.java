package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.engenharia.operacoes.desenhos.application.event.DesenhoExcluidoEvent;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.DeleteDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJobArquivo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoJobNotFoundException;

@Service
public class DeleteDesenhoService implements DeleteDesenhoUseCase {

    private final DesenhoJobRepository jobs;
    private final ApplicationEventPublisher eventos;

    public DeleteDesenhoService(DesenhoJobRepository jobs, ApplicationEventPublisher eventos) {
        this.jobs = jobs;
        this.eventos = eventos;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        DesenhoJob job = jobs.findById(id).orElseThrow(
                () -> new DesenhoJobNotFoundException(id));
        job.garantirPodeExcluir();
        jobs.deleteById(job.getId());
        eventos.publishEvent(new DesenhoExcluidoEvent(
                job.getArquivos().stream().map(DesenhoJobArquivo::chaveArmazenamento).toList()));
    }

}
