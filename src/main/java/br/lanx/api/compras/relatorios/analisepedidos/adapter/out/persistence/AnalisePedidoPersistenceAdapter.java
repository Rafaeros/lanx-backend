package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import br.lanx.api.cadastros.fornecedores.Fornecedor;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.cadastros.unidademedida.adapter.out.persistence.UnidadeMedidaEntity;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ConsultarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.SalvarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class AnalisePedidoPersistenceAdapter implements SalvarAnalisePedidosPort, ConsultarAnalisePedidosPort {

    private final AnalisePedidoRepository repository;
    private final AnalisePedidoPersistenceMapper mapper;
    private final EntityManager entityManager;

    public AnalisePedidoPersistenceAdapter(AnalisePedidoRepository repository, AnalisePedidoPersistenceMapper mapper,
            EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    /**
     * Os ids vêm da normalização; getReference evita um SELECT por item só para
     * montar a FK.
     */
    @Override
    public void salvar(List<ItemAnalisePedido> itens) {
        var entities = itens.stream()
                .map(item -> mapper.toEntity(item,
                        entityManager.getReference(Material.class,
                                Objects.requireNonNull(item.materialId(), "materialId não normalizado")),
                        entityManager.getReference(UnidadeMedidaEntity.class,
                                Objects.requireNonNull(item.unidadeMedidaId(), "unidadeMedidaId não normalizado")),
                        item.fornecedorId() == null ? null
                                : entityManager.getReference(Fornecedor.class, item.fornecedorId())))
                .toList();
        repository.saveAll(entities);
    }

    // A ordenação (emissão, pedido, código do material) está na própria query
    @Override
    public PageResult<ItemAnalisePedido> buscar(ConsultaAnalisePedidos consulta, PageQuery pagina) {
        var pageRequest = PageRequest.of(pagina.page(), pagina.size());
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
