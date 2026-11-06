package br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions;

import java.util.UUID;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

/** Job (ou arquivo do job) inexistente; o adaptador web devolve 404. */
public class DesenhoJobNotFoundException extends DomainException {

    public DesenhoJobNotFoundException(UUID jobId) {
        super(ErrorType.NOT_FOUND, "Desenho " + jobId + " não encontrado");
    }

    public DesenhoJobNotFoundException(UUID jobId, UUID arquivoId) {
        super(ErrorType.NOT_FOUND, "Arquivo " + arquivoId + " não encontrado no desenho " + jobId);
    }
}
