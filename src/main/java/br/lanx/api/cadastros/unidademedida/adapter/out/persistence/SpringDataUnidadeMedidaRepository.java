package br.lanx.api.cadastros.unidademedida.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataUnidadeMedidaRepository extends JpaRepository<UnidadeMedidaEntity, UUID> {

        @Query("SELECT u FROM UnidadeMedidaEntity u WHERE "
                        + "(:filtro is NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :filtro, '%'))) "
                        + "OR LOWER(u.sigla) LIKE LOWER(CONCAT('%', :filtro, '%'))")
        Page<UnidadeMedidaEntity> search(@Param("filtro") String filtro, Pageable pageable);

        boolean existsById(UUID id);

        boolean existsByNome(String nome);

        boolean existsBySigla(String sigla);

        Optional<UnidadeMedidaEntity> findById(UUID id);

        Optional<UnidadeMedidaEntity> findFirstByNomeIgnoreCase(String nome);

        Optional<UnidadeMedidaEntity> findBySigla(String sigla);

        @Query("UPDATE UnidadeMedidaEntity u SET u.nome = :nome, u.sigla = :sigla WHERE u.id = :id")
        Optional<UnidadeMedidaEntity> update(@Param("id") UUID id, @Param("nome") String nome,
                        @Param("sigla") String sigla);
}
