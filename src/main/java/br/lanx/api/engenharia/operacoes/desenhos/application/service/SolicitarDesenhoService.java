package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.AnaliseListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.application.event.DesenhoSolicitadoEvent;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.AnalisarListaCorteUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJobArquivo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.TipoArquivoDesenho;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.ProblemaListaCorte;
import br.lanx.api.shared.storage.AnexoStoragePort;

@Service
class SolicitarDesenhoService implements SolicitarDesenhoUseCase {

    private static final Logger log = LoggerFactory.getLogger(SolicitarDesenhoService.class);

    private final DesenhoJobRepository jobs;
    private final AnexoStoragePort storage;
    private final ApplicationEventPublisher eventos;
    private final AnalisarListaCorteUseCase analise;
    private final Clock clock;

    SolicitarDesenhoService(DesenhoJobRepository jobs, AnexoStoragePort storage, ApplicationEventPublisher eventos,
            AnalisarListaCorteUseCase analise, Clock clock) {
        this.jobs = jobs;
        this.analise = analise;
        this.storage = storage;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UUID solicitar(SolicitarDesenhoCommand cmd) {
        // 1. domínio: valida e cria o job com as chaves dos arquivos
        DesenhoJob job = DesenhoJob.solicitar(cmd.solicitanteId(), cmd.projeto(), cmd.opcoes(),
                cmd.projectOverride(), cmd.listaCorte() == null ? null : cmd.listaCorte().descricao(),
                cmd.desenhoCliente() == null ? null : cmd.desenhoCliente().descricao(), clock);

        // 1b. lista de corte: conector no padrão de vias com cavidade inexistente/duplicada não vai para a fila
        recusarSeTiverErro(analise.analisar(cmd.listaCorte()));

        // 2. storage: os bytes vão para as chaves que o domínio gerou
        List<String> gravados = new ArrayList<>();
        try {
            gravar(job, TipoArquivoDesenho.LISTA_CORTE, cmd.listaCorte(), gravados);
            if (cmd.desenhoCliente() != null)
                gravar(job, TipoArquivoDesenho.DESENHO_CLIENTE, cmd.desenhoCliente(), gravados);

            // 3. banco
            jobs.save(job);
        } catch (RuntimeException e) {
            // sem isto os arquivos ficariam órfãos na pasta de anexos
            gravados.forEach(this::removerSemFalhar);
            throw e;
        }

        // 4. fila: o adapter publica só depois do commit (DesenhoSolicitadoEvent)
        eventos.publishEvent(new DesenhoSolicitadoEvent(job));
        log.info("Desenho {} solicitado: projeto {}", job.getId(), job.getProjeto());
        return job.getId();
    }

    private static void recusarSeTiverErro(AnaliseListaCorte resultado) {
        if (!resultado.temErro())
            return;
        List<String> erros = resultado.problemas().stream()
                .filter(p -> p.nivel() == ProblemaListaCorte.Nivel.ERRO)
                .map(ProblemaListaCorte::mensagem)
                .toList();
        throw new DesenhoInvalidoException("Lista de corte com " + erros.size()
                + " erro(s) nos conectores; corrija antes de gerar:\n" + String.join("\n", erros));
    }

    private void gravar(DesenhoJob job, TipoArquivoDesenho tipo, Upload upload, List<String> gravados) {
        DesenhoJobArquivo arquivo = job.arquivo(tipo).orElseThrow();
        storage.salvar(arquivo.chaveArmazenamento(), upload.conteudo().get());
        gravados.add(arquivo.chaveArmazenamento());
    }

    private void removerSemFalhar(String chave) {
        try {
            storage.remover(chave);
        } catch (RuntimeException e) {
            log.warn("Não foi possível remover o anexo órfão {}", chave, e);
        }
    }
}
