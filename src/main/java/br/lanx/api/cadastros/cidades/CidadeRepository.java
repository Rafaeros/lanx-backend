package br.lanx.api.cadastros.cidades;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CidadeRepository extends JpaRepository<Cidade, Integer> {
    @EntityGraph(attributePaths = "estado")
    Page<Cidade> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
