package br.lanx.api.compras.relatorios.analisepedidos.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ConsultarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ConsultarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

@Service
public class ConsultarAnalisePedidosService implements ConsultarAnalisePedidosUseCase {

    private final ConsultarAnalisePedidosPort consultarAnalisePedidosPort;

    public ConsultarAnalisePedidosService(ConsultarAnalisePedidosPort consultarAnalisePedidosPort) {
        this.consultarAnalisePedidosPort = consultarAnalisePedidosPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemAnalisePedido> consultar(FiltroPedidos filtro) {
        return consultarAnalisePedidosPort.buscarPorPeriodo(filtro);
    }

}
