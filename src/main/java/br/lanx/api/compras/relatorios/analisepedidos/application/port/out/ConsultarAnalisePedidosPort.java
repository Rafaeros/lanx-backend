package br.lanx.api.compras.relatorios.analisepedidos.application.port.out;

import java.util.List;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

public interface ConsultarAnalisePedidosPort {

    PageResult<ItemAnalisePedido> buscar(ConsultaAnalisePedidos consulta, PageQuery pagina);

    List<IndicadoresItem> buscarIndicadores(ConsultaAnalisePedidos consulta);
}
