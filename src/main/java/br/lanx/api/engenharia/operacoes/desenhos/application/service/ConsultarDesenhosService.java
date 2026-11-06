package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.ArquivoDesenhoDownload;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoDetalhe;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoResumo;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.ConsultarDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.ConsultarDesenhosPort;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJobArquivo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoJobNotFoundException;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.storage.AnexoStoragePort;

@Service
@Transactional(readOnly = true)
class ConsultarDesenhosService implements ConsultarDesenhoUseCase {

    private final ConsultarDesenhosPort consultas;
    private final DesenhoJobRepository jobs;
    private final AnexoStoragePort storage;

    ConsultarDesenhosService(ConsultarDesenhosPort consultas, DesenhoJobRepository jobs, AnexoStoragePort storage) {
        this.consultas = consultas;
        this.jobs = jobs;
        this.storage = storage;
    }

    @Override
    public PageResult<DesenhoResumo> listar(ListarDesenhosQuery q) {
        return consultas.listar(q.solicitanteId(), q.status(), q.projeto(), q.pagina());
    }

    @Override
    public DesenhoDetalhe buscar(UUID jobId) {
        return consultas.detalhe(jobId).orElseThrow(() -> new DesenhoJobNotFoundException(jobId));
    }

    @Override
    public ArquivoDesenhoDownload baixarArquivo(UUID jobId, UUID arquivoId) {
        // a chave de armazenamento fica só aqui dentro: o front pede pelo id do arquivo
        DesenhoJobArquivo arquivo = jobs.findById(jobId)
                .orElseThrow(() -> new DesenhoJobNotFoundException(jobId))
                .getArquivos().stream()
                .filter(a -> a.id().equals(arquivoId))
                .findFirst()
                .orElseThrow(() -> new DesenhoJobNotFoundException(jobId, arquivoId));
        return new ArquivoDesenhoDownload(arquivo.nomeArquivo(), arquivo.contentType(), arquivo.tamanhoBytes(),
                storage.carregar(arquivo.chaveArmazenamento()));
    }
}
