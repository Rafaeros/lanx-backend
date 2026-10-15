package br.lanx.api.compras.relatorios.analisepedidos.application.service;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ImportarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeMaterialPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeUnidadeMedidaPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ObterPedidosExternosPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.SalvarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ImportarAnalisePedidosService implements ImportarAnalisePedidosUseCase {

    private final ObterPedidosExternosPort obterPedidosExternosPort;
    private final NormalizeUnidadeMedidaPort normalizarUnidadeMedidaPort;
    private final NormalizeMaterialPort normalizarMaterialPort;
    private final SalvarAnalisePedidosPort salvarAnalisePedidosPort;

    public ImportarAnalisePedidosService(ObterPedidosExternosPort obterPedidosExternosPort,
            NormalizeUnidadeMedidaPort normalizarUnidadeMedidaPort, NormalizeMaterialPort normalizarMaterialPort,
            SalvarAnalisePedidosPort salvarAnalisePedidosPort) {
        this.obterPedidosExternosPort = obterPedidosExternosPort;
        this.normalizarUnidadeMedidaPort = normalizarUnidadeMedidaPort;
        this.normalizarMaterialPort = normalizarMaterialPort;
        this.salvarAnalisePedidosPort = salvarAnalisePedidosPort;
    }

    @Override
    @Transactional
    public int importar(FiltroPedidos filtro) {
        List<ItemAnalisePedido> pedidos = obterPedidosExternosPort.obterPedidos(filtro).stream()
                .map(this::normalizar)
                .toList();

        // Reimportar o mesmo período substitui os itens em vez de duplicá-los
        salvarAnalisePedidosPort.removerPorPeriodo(filtro);
        salvarAnalisePedidosPort.salvar(pedidos);
        return pedidos.size();
    }

    private ItemAnalisePedido normalizar(ItemAnalisePedido item) {

        UUID unidadeMedidaId = normalizarUnidadeMedidaPort.obterOuCriar(
                item.unidadeMedida(),
                item.unidadeMedida());

        UUID materialId = normalizarMaterialPort.obterOuCriar(
                item.codigoItem(),
                item.descricaoItem(),
                unidadeMedidaId);

        return item
                .withUnidadeMedidaId(unidadeMedidaId)
                .withMaterialId(materialId);
    }
}