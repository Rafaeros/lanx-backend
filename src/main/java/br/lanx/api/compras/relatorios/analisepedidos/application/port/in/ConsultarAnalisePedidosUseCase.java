package br.lanx.api.compras.relatorios.analisepedidos.application.port.in;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ResumoAnalisePedidos;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

public interface ConsultarAnalisePedidosUseCase {

    PageResult<ItemAnalisePedido> consultar(ConsultaAnalisePedidos consulta, PageQuery pagina);

    /** Totais de todos os itens que atendem aos filtros, independentemente da página. */
    ResumoAnalisePedidos resumir(ConsultaAnalisePedidos consulta);
}
