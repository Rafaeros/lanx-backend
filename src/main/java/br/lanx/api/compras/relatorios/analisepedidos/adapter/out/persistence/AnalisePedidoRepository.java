package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalisePedidoRepository extends JpaRepository<AnalisePedidoEntity, UUID> {

    List<AnalisePedidoEntity> findByDataEmissaoPcBetweenOrderByDataEmissaoPcAscPedidoDeCompraAsc(LocalDate inicio,
            LocalDate fim);

    // Delete físico: os itens são reimportados do ERP, não faz sentido acumular soft-deletes
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM compras.analise_pedido_item WHERE data_emissao_pc BETWEEN :inicio AND :fim", nativeQuery = true)
    int deleteByDataEmissaoPcBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

}
