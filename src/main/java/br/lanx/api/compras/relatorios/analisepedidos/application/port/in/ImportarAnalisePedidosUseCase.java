package br.lanx.api.compras.relatorios.analisepedidos.application.port.in;

import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;

public interface ImportarAnalisePedidosUseCase {
    /** Substitui os itens do período pelos do ERP e devolve quantos foram importados. */
    int importar(FiltroPedidos filtro);
}