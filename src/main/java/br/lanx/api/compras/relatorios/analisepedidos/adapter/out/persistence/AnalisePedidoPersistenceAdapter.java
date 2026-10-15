package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ConsultarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.SalvarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalisePedidoPersistenceAdapter implements SalvarAnalisePedidosPort, ConsultarAnalisePedidosPort {

    private final AnalisePedidoRepository repository;
    private final AnalisePedidoPersistenceMapper mapper;

    

    public AnalisePedidoPersistenceAdapter(AnalisePedidoRepository repository, AnalisePedidoPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }



    @Override
    public void salvar(List<ItemAnalisePedido> itens) {
        var entities = itens.stream()
            .map(mapper::toEntity)
            .toList();
        repository.saveAll(entities);
    }

    @Override
    public List<ItemAnalisePedido> buscarPorPeriodo(FiltroPedidos filtro) {
        return repository
                .findByDataEmissaoPcBetweenOrderByDataEmissaoPcAscPedidoDeCompraAsc(filtro.dataInicio(), filtro.dataFim())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void removerPorPeriodo(FiltroPedidos filtro) {
        repository.deleteByDataEmissaoPcBetween(filtro.dataInicio(), filtro.dataFim());
    }
}