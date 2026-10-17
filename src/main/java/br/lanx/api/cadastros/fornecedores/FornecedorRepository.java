package br.lanx.api.cadastros.fornecedores;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository extends JpaRepository<Fornecedor, UUID> {

    Optional<Fornecedor> findByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    /** Traz o país junto: a listagem mostra o país de cada linha. */
    @Override
    @EntityGraph(attributePaths = "pais")
    Page<Fornecedor> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "pais")
    Page<Fornecedor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
