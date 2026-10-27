package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.lanx.api.cadastros.rateios.Rateio;
import br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem;

public interface AnalisePedidoRepository extends JpaRepository<AnalisePedidoEntity, UUID> {

        /**
         * Joins com os cadastros normalizados. Left join para não esconder itens cujo
         * material/unidade foi excluído logicamente (@SQLRestriction) ou sem
         * fornecedor.
         */
        String JOINS = """
                         left join e.material m
                         left join e.unidadeMedida u
                         left join e.fornecedor f
                         left join e.comprador c
                         left join e.rateio r
                        """;

        /**
         * Mesmos joins, já trazendo as associações para o mapper (evita N+1 na
         * listagem).
         */
        String JOINS_FETCH = """
                         left join fetch e.material m
                         left join fetch e.unidadeMedida u
                         left join fetch e.fornecedor f
                         left join fetch f.pais p
                         left join fetch e.comprador c
                         left join fetch e.rateio r
                        """;

        /**
         * Filtro compartilhado pela listagem e pelo resumo. Parâmetros vazios ('')
         * desligam o filtro (evita null tipado, que o Postgres não infere).
         * A regra de situação espelha domain.Situacao.
         */
        String FILTRO = """
                         where e.dataEmissaoPc between :inicio and :fim
                           and (:busca = ''
                                or lower(coalesce(e.pedidoDeCompra, '')) like :busca
                                or lower(coalesce(e.requisicaoDeCompra, '')) like :busca
                                or lower(coalesce(f.nome, '')) like :busca
                                or lower(coalesce(m.codigo, '')) like :busca
                                or lower(coalesce(m.descricao, '')) like :busca)
                           and (:situacao = ''
                                or (:situacao = 'PENDENTE' and coalesce(e.quantidadeAtendida, 0) <= 0)
                                or (:situacao = 'PARCIAL' and coalesce(e.quantidadeAtendida, 0) > 0 and coalesce(e.saldo, 0) > 0)
                                or (:situacao = 'ATENDIDO' and coalesce(e.quantidadeAtendida, 0) > 0 and coalesce(e.saldo, 0) <= 0))
                           and (:todosRateios = true or r.id in :rateios)
                        """;

        @Query(value = "select e from AnalisePedidoEntity e" + JOINS_FETCH + FILTRO
                        + " order by e.dataEmissaoPc, e.pedidoDeCompra, m.codigo, e.id", countQuery = "select count(e) from AnalisePedidoEntity e"
                                        + JOINS + FILTRO)
        Page<AnalisePedidoEntity> consultar(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
                        @Param("busca") String busca, @Param("situacao") String situacao,
                        @Param("todosRateios") boolean todosRateios, @Param("rateios") Collection<UUID> rateios, Pageable pageable);

        @Query("select new br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem("
                        + "e.valorTotal, e.quantidadeAtendida, e.saldo, e.dataEmissaoPc, e.dataEntregaPc) "
                        + "from AnalisePedidoEntity e" + JOINS + FILTRO)
        List<IndicadoresItem> indicadores(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
                        @Param("busca") String busca, @Param("situacao") String situacao,
                        @Param("todosRateios") boolean todosRateios, @Param("rateios") Collection<UUID> rateios);

        @Query("""
                        select e
                        from AnalisePedidoEntity e
                        left join fetch e.fornecedor f
                        where e.dataPrevisaoEntregaPc between :inicio and :fim
                        order by e.dataPrevisaoEntregaPc, e.id
                        """)
        List<AnalisePedidoEntity> buscarEntregasParaIqf(
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);

        @Query("""
                        select e
                        from AnalisePedidoEntity e
                        left join fetch e.fornecedor f
                        join e.rateio r
                        where e.dataPrevisaoEntregaPc between :inicio and :fim
                          and r.id in :rateios
                        order by e.dataPrevisaoEntregaPc, e.id
                        """)
        List<AnalisePedidoEntity> buscarEntregasParaIqfPorRateio(
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim,
                        @Param("rateios") Collection<UUID> rateios);

        /** Rateios referenciados por algum item importado (para os filtros da análise e do IQF). */
        @Query("""
                        select distinct r
                        from AnalisePedidoEntity e
                        join e.rateio r
                        order by r.nome
                        """)
        List<Rateio> listarRateios();

        boolean existsByCompradorId(UUID compradorId);

        boolean existsByRateioId(UUID rateioId);

        /** Todos os itens dos filtros, sem paginação (exportação da planilha). */
        @Query("select e from AnalisePedidoEntity e" + JOINS_FETCH + FILTRO
                        + " order by e.dataEmissaoPc, e.pedidoDeCompra, m.codigo, e.id")
        List<AnalisePedidoEntity> listar(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
                        @Param("busca") String busca, @Param("situacao") String situacao,
                        @Param("todosRateios") boolean todosRateios, @Param("rateios") Collection<UUID> rateios);

        // Delete físico: os itens são reimportados do ERP, não faz sentido acumular
        // soft-deletes
        @Modifying(flushAutomatically = true, clearAutomatically = true)
        @Query(value = "DELETE FROM compras.analise_pedidos_itens WHERE data_emissao_pc BETWEEN :inicio AND :fim", nativeQuery = true)
        int deleteByDataEmissaoPcBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

}
