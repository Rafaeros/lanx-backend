package br.lanx.api.compras.relatorios.analisepedidos.application.port.in;

import java.util.List;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.compras.relatorios.analisepedidos.domain.RateioFiltro;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ResumoAnalisePedidos;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

public interface ConsultarAnalisePedidosUseCase {

    PageResult<ItemAnalisePedido> consultar(ConsultaAnalisePedidos consulta, PageQuery pagina);

    /** Totais de todos os itens que atendem aos filtros, independentemente da página. */
    ResumoAnalisePedidos resumir(ConsultaAnalisePedidos consulta);

    /** Todos os itens que atendem aos filtros, sem paginação (para a planilha). */
    List<ItemAnalisePedido> listarTodos(ConsultaAnalisePedidos consulta);

    /** Rateios presentes nos itens importados, por nome, para o filtro da tela. */
    List<RateioFiltro> listarRateios();
}
