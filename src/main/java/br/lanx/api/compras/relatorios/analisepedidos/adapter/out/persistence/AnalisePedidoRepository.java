package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem;

public interface AnalisePedidoRepository extends JpaRepository<AnalisePedidoEntity, UUID> {

    /**
     * Filtro compartilhado pela listagem e pelo resumo. Parâmetros vazios ('')
     * desligam o filtro (evita null tipado, que o Postgres não infere).
     * A regra de situação espelha domain.Situacao.
     */
    String FILTRO = """
             where e.dataEmissaoPc between :inicio and :fim
               and (:busca = ''
                    or lower(e.pedidoDeCompra) like :busca
                    or lower(coalesce(e.requisicaoDeCompra, '')) like :busca
                    or lower(coalesce(e.fornecedor, '')) like :busca
                    or lower(coalesce(e.codigoItem, '')) like :busca
                    or lower(coalesce(e.descricaoItem, '')) like :busca)
               and (:situacao = ''
                    or (:situacao = 'PENDENTE' and coalesce(e.quantidadeAtendida, 0) <= 0)
                    or (:situacao = 'PARCIAL' and coalesce(e.quantidadeAtendida, 0) > 0 and coalesce(e.saldo, 0) > 0)
                    or (:situacao = 'ATENDIDO' and coalesce(e.quantidadeAtendida, 0) > 0 and coalesce(e.saldo, 0) <= 0))
            """;

    @Query(value = "select e from AnalisePedidoEntity e" + FILTRO,
            countQuery = "select count(e) from AnalisePedidoEntity e" + FILTRO)
    Page<AnalisePedidoEntity> consultar(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
            @Param("busca") String busca, @Param("situacao") String situacao, Pageable pageable);

    @Query("select new br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem("
            + "e.valorTotal, e.quantidadeAtendida, e.saldo, e.dataEmissaoPc, e.dataEntregaPc) "
            + "from AnalisePedidoEntity e" + FILTRO)
    List<IndicadoresItem> indicadores(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim,
            @Param("busca") String busca, @Param("situacao") String situacao);

    // Delete físico: os itens são reimportados do ERP, não faz sentido acumular soft-deletes
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM compras.analise_pedido_item WHERE data_emissao_pc BETWEEN :inicio AND :fim", nativeQuery = true)
    int deleteByDataEmissaoPcBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

}
