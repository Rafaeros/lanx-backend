package br.lanx.api.cadastros.tiponaoconformidade;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoNaoConformidadeRepository extends JpaRepository<TipoNaoConformidade, UUID> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    Page<TipoNaoConformidade> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
