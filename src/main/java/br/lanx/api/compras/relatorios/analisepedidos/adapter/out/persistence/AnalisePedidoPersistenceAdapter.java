package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ConsultarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.SalvarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class AnalisePedidoPersistenceAdapter implements SalvarAnalisePedidosPort, ConsultarAnalisePedidosPort {

    private static final Sort ORDENACAO = Sort.by("dataEmissaoPc", "pedidoDeCompra", "codigoItem");

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
    public PageResult<ItemAnalisePedido> buscar(ConsultaAnalisePedidos consulta, PageQuery pagina) {
        var pageRequest = PageRequest.of(pagina.page(), pagina.size(), ORDENACAO);
        var page = repository.consultar(consulta.dataInicio(), consulta.dataFim(), busca(consulta),
                situacao(consulta), pageRequest);
        return PageResult.from(page, mapper::toDomain);
    }

    @Override
    public List<IndicadoresItem> buscarIndicadores(ConsultaAnalisePedidos consulta) {
        return repository.indicadores(consulta.dataInicio(), consulta.dataFim(), busca(consulta), situacao(consulta));
    }

    private static String busca(ConsultaAnalisePedidos consulta) {
        return consulta.temBusca() ? "%" + consulta.busca().toLowerCase(Locale.ROOT) + "%" : "";
    }

    private static String situacao(ConsultaAnalisePedidos consulta) {
        return consulta.situacao() == null ? "" : consulta.situacao().name();
    }

    @Override
    public void removerPorPeriodo(FiltroPedidos filtro) {
        repository.deleteByDataEmissaoPcBetween(filtro.dataInicio(), filtro.dataFim());
    }
}