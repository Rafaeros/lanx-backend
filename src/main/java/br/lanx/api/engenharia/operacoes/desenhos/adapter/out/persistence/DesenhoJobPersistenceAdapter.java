package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;

/** Escrita: salva e carrega o agregado {@link DesenhoJob}. */
@Repository
class DesenhoJobPersistenceAdapter implements DesenhoJobRepository {

    private final SpringDataDesenhoJobRepository jpa;
    private final DesenhoJobMapper mapper;

    DesenhoJobPersistenceAdapter(SpringDataDesenhoJobRepository jpa, DesenhoJobMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public DesenhoJob save(DesenhoJob job) {
        Optional<DesenhoJobEntity> existente = jpa.findById(job.getId());

        if (existente.isEmpty()) { // ---- INSERT ----
            return mapper.toDomain(jpa.save(mapper.toNewEntity(job)));
        }

        DesenhoJobEntity e = existente.get(); // ---- UPDATE ----
        // o @Version protege dois UPDATEs ao mesmo tempo; este if cobre o job lido em outra transação
        if (!Objects.equals(e.getVersion(), job.getVersion()))
            throw new ObjectOptimisticLockingFailureException(DesenhoJobEntity.class, job.getId());

        mapper.copyState(job, e); // muda os campos e marca os arquivos que saíram para DELETE
        jpa.flush(); // executa o DELETE agora (o zip antigo sai antes de o novo entrar)
        mapper.addNewFiles(job, e);
        jpa.flush(); // grava os novos e incrementa a version
        return mapper.toDomain(e);
    }

    @Override
    public Optional<DesenhoJob> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }
}
