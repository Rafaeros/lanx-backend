package br.lanx.api.compras.relatorios.analisepedidos.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ConsultarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ConsultarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.compras.relatorios.analisepedidos.domain.RateioFiltro;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ResumoAnalisePedidos;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class ConsultarAnalisePedidosService implements ConsultarAnalisePedidosUseCase {

    private final ConsultarAnalisePedidosPort consultarAnalisePedidosPort;

    public ConsultarAnalisePedidosService(ConsultarAnalisePedidosPort consultarAnalisePedidosPort) {
        this.consultarAnalisePedidosPort = consultarAnalisePedidosPort;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ItemAnalisePedido> consultar(ConsultaAnalisePedidos consulta, PageQuery pagina) {
        return consultarAnalisePedidosPort.buscar(consulta, pagina);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumoAnalisePedidos resumir(ConsultaAnalisePedidos consulta) {
        return ResumoAnalisePedidos.de(consultarAnalisePedidosPort.buscarIndicadores(consulta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemAnalisePedido> listarTodos(ConsultaAnalisePedidos consulta) {
        return consultarAnalisePedidosPort.buscarTodos(consulta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RateioFiltro> listarRateios() {
        return consultarAnalisePedidosPort.listarRateios();
    }
}
