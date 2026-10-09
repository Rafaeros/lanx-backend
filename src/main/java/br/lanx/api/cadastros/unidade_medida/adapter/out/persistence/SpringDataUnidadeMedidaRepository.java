package br.lanx.api.cadastros.unidade_medida.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUnidadeMedidaRepository extends JpaRepository<UnidadeMedidaEntity, UUID> {
    boolean existsByNome(String nome);

    boolean existsBySigla(String sigla);

    Optional<UnidadeMedidaEntity> findByNome(String nome);

    Optional<UnidadeMedidaEntity> findBySigla(String sigla);
}
