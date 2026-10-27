package br.lanx.api.producao.operacoes.ordensdeproducao;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdemDeProducaoRepository extends JpaRepository<OrdemDeProducao, UUID> {

    /**
     * Busca vazia ('') = todas; senão, código da OP, nome do cliente ou código/descrição
     * do material. Left join para não esconder a OP cujo material foi excluído logicamente.
     */
    String FILTRO = """
             where :busca = ''
                or lower(o.codigo) like :busca
                or lower(coalesce(c.nome, '')) like :busca
                or lower(coalesce(m.codigo, '')) like :busca
                or lower(coalesce(m.descricao, '')) like :busca
            """;

    @Query(value = """
            select o from OrdemDeProducao o
             left join fetch o.cliente c
             left join fetch o.material m
             left join fetch m.unidadeMedida
            """ + FILTRO, countQuery = """
            select count(o) from OrdemDeProducao o
             left join o.cliente c
             left join o.material m
            """ + FILTRO)
    Page<OrdemDeProducao> buscar(@Param("busca") String busca, Pageable pageable);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, UUID id);

    boolean existsByClienteId(UUID clienteId);
}
