package br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions;

import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;
import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class TransicaoDesenhoInvalidaException extends DomainException {

    private final UUID jobId;
    private final StatusDesenho atual;
    private final StatusDesenho destino;

    public TransicaoDesenhoInvalidaException(UUID jobId, StatusDesenho atual, StatusDesenho destino) {
        super(ErrorType.BUSINESS_RULE,
                "Não é possível alterar o desenho de \"" + atual.rotulo() + "\" para \"" + destino.rotulo() + "\"");
        this.jobId = jobId;
        this.atual = atual;
        this.destino = destino;
    }

    public UUID jobId() {
        return jobId;
    }

    public StatusDesenho atual() {
        return atual;
    }

    public StatusDesenho destino() {
        return destino;
    }
}
