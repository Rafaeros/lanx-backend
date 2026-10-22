package br.lanx.api.cadastros.operadores;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperadorRepository extends JpaRepository<Operador, UUID> {

    @Override
    @EntityGraph(attributePaths = "departamento")
    Page<Operador> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "departamento")
    Page<Operador> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "departamento")
    Optional<Operador> findById(UUID id);

    Optional<Operador> findByNome(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    boolean existsByDepartamentoId(UUID departamentoId);

    boolean existsByUserId(UUID userId);

    boolean existsByUserIdAndIdNot(UUID userId, UUID id);
}
