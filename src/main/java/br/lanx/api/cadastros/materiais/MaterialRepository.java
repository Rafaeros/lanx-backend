package br.lanx.api.cadastros.materiais;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaterialRepository extends JpaRepository<Material, UUID>, JpaSpecificationExecutor<Material> {

    @Override
    @EntityGraph(attributePaths = { "unidadeMedida" })
    Page<Material> findAll(Specification<Material> spec, Pageable pageable);

    @EntityGraph(attributePaths = { "unidadeMedida" })
    Optional<Material> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

}
