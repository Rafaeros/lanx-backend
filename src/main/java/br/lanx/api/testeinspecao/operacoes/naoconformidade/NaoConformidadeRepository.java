package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NaoConformidadeRepository extends JpaRepository<NaoConformidade, UUID> {

    /**
     * Left join para não esconder a NC cujo tipo/departamento/OP/operador (ou o
     * cliente/material da OP) foi excluído logicamente (@SQLRestriction). Anexos
     * ficam de fora: fetch de coleção com paginação faz o Hibernate paginar em memória.
     */
    String FETCH = """
            select nc from NaoConformidade nc
             left join fetch nc.tipo t
             left join fetch nc.departamento d
             left join fetch nc.ordemDeProducao op
             left join fetch op.cliente c
             left join fetch op.material m
             left join fetch nc.operador o
            """;

    String JOINS_COUNT = """
            select count(nc) from NaoConformidade nc
             left join nc.tipo t
             left join nc.departamento d
             left join nc.ordemDeProducao op
             left join op.cliente c
             left join op.material m
             left join nc.operador o
            """;

    /**
     * Período pela data da NC. Busca vazia ('') = todas; senão, código da OP,
     * cliente, produto, tipo, departamento ou operador.
     */
    String FILTRO = """
             where nc.dataNaoConformidade between :inicio and :fim
               and (:busca = ''
                or lower(coalesce(op.codigo, '')) like :busca
                or lower(coalesce(c.nome, '')) like :busca
                or lower(coalesce(m.codigo, '')) like :busca
                or lower(coalesce(m.descricao, '')) like :busca
                or lower(coalesce(t.nome, '')) like :busca
                or lower(coalesce(d.nome, '')) like :busca
                or lower(coalesce(o.nome, '')) like :busca)
            """;

    @Query(value = FETCH + FILTRO, countQuery = JOINS_COUNT + FILTRO)
    Page<NaoConformidade> buscar(@Param("busca") String busca, @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim, Pageable pageable);

    /**
     * Mesmo filtro da busca, sem paginação: para a planilha. Ordem fixa aqui (e
     * não por Sort) para ordenar pelo alias do left join, sem um join implícito
     * que esconderia a NC cuja OP foi excluída.
     */
    @Query(FETCH + FILTRO + " order by nc.dataNaoConformidade, op.codigo, nc.createdAt")
    List<NaoConformidade> listar(@Param("busca") String busca, @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @Query(FETCH + " where nc.id = :id")
    Optional<NaoConformidade> buscarDetalhe(@Param("id") UUID id);

    boolean existsByTipo_Id(UUID tipoNaoConformidadeId);

    boolean existsByDepartamento_Id(UUID departamentoId);
}
