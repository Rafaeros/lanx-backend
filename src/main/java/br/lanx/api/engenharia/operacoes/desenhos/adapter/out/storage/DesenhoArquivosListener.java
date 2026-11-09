package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import br.lanx.api.engenharia.operacoes.desenhos.application.event.DesenhoExcluidoEvent;
import br.lanx.api.shared.storage.AnexoStoragePort;

@Component
public class DesenhoArquivosListener {
    private static final Logger log = LoggerFactory.getLogger(DesenhoArquivosListener.class);

    private final AnexoStoragePort storage;

    public DesenhoArquivosListener(AnexoStoragePort storage) {
        this.storage = storage;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void aoExcluir(DesenhoExcluidoEvent evento) {
        evento.chaves().forEach(this::removerSemFalhar);
    }

    private void removerSemFalhar(String chave) {
        try {
            storage.remover(chave);
        } catch (RuntimeException e) {
            log.warn("Não foi possivel remover o anexo orfão {}", chave, e);
        }
    }
}
