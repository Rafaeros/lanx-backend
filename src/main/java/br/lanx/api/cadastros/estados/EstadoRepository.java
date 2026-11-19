package br.lanx.api.cadastros.estados;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    @Query("""
        SELECT e from Estado e
        WHERE :busca = ''
            OR lower(e.nome) like :busca
            OR lower(e.uf) like :busca    
        ORDER BY e.nome
            """)
    List<Estado> buscar(@Param("busca") String busca);

    Optional<Estado> findByNomeIgnoreCase(String nome);
}
