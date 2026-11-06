package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;

/** O repositório do Spring Data: trabalha com a Entity e fica escondido atrás dos adapters. */
interface SpringDataDesenhoJobRepository
        extends JpaRepository<DesenhoJobEntity, UUID>, JpaSpecificationExecutor<DesenhoJobEntity> {

    /**
     * Filtros opcionais do histórico. Specification em vez de JPQL com {@code :param is null}: no Postgres
     * esse truque falha com parâmetro nulo de texto ("could not determine data type of parameter").
     */
    static Specification<DesenhoJobEntity> filtro(UUID solicitanteId, StatusDesenho status, String projeto) {
        Specification<DesenhoJobEntity> spec = Specification.unrestricted();
        if (solicitanteId != null)
            spec = spec.and((root, q, cb) -> cb.equal(root.get("solicitanteId"), solicitanteId));
        if (status != null)
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        if (projeto != null && !projeto.isBlank()) {
            String trecho = "%" + projeto.trim().toUpperCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(root.get("projeto"), trecho)); // projeto já é gravado em maiúsculas
        }
        return spec;
    }
}
