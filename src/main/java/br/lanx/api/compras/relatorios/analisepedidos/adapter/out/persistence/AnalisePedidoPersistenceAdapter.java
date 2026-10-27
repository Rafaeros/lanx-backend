package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import br.lanx.api.cadastros.compradores.Comprador;
import br.lanx.api.cadastros.fornecedores.Fornecedor;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.cadastros.rateios.Rateio;
import br.lanx.api.cadastros.unidademedida.adapter.out.persistence.UnidadeMedidaEntity;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ConsultarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.SalvarAnalisePedidosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.IndicadoresItem;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.compras.relatorios.analisepedidos.domain.RateioFiltro;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
public class AnalisePedidoPersistenceAdapter implements SalvarAnalisePedidosPort, ConsultarAnalisePedidosPort {

    private static final Set<UUID> SEM_RATEIOS = Set.of(new UUID(0, 0));

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
                        referencia(Fornecedor.class, item.fornecedorId()),
                        referencia(Comprador.class, item.compradorId()),
                        referencia(Rateio.class, item.rateioId())))
                .toList();
        repository.saveAll(entities);
    }

    /** Vínculo opcional: id nulo = item sem esse cadastro. */
    private <T> T referencia(Class<T> tipo, UUID id) {
        return id == null ? null : entityManager.getReference(tipo, id);
    }

    // A ordenação (emissão, pedido, código do material) está na própria query
    @Override
    public PageResult<ItemAnalisePedido> buscar(ConsultaAnalisePedidos consulta, PageQuery pagina) {
        var pageRequest = PageRequest.of(pagina.page(), pagina.size());
        var page = repository.consultar(consulta.dataInicio(), consulta.dataFim(), busca(consulta),
                situacao(consulta), !consulta.filtraRateios(), rateios(consulta), pageRequest);
        return PageResult.from(page, mapper::toDomain);
    }

    @Override
    public List<IndicadoresItem> buscarIndicadores(ConsultaAnalisePedidos consulta) {
        return repository.indicadores(consulta.dataInicio(), consulta.dataFim(), busca(consulta), situacao(consulta),
                !consulta.filtraRateios(), rateios(consulta));
    }

    @Override
    public List<ItemAnalisePedido> buscarTodos(ConsultaAnalisePedidos consulta) {
        return repository.listar(consulta.dataInicio(), consulta.dataFim(), busca(consulta), situacao(consulta),
                !consulta.filtraRateios(), rateios(consulta))
                .stream().map(mapper::toDomain).toList();
    }

    private static String busca(ConsultaAnalisePedidos consulta) {
        return consulta.temBusca() ? "%" + consulta.busca().toLowerCase(Locale.ROOT) + "%" : "";
    }

    @Override
    public List<RateioFiltro> listarRateios() {
        return repository.listarRateios().stream().map(r -> new RateioFiltro(r.getId(), r.getNome())).toList();
    }

    /** "in ()" vazio não é SQL válido: sem filtro, manda um id que não casa e desliga pelo todosRateios. */
    private static Collection<UUID> rateios(ConsultaAnalisePedidos consulta) {
        return consulta.filtraRateios() ? consulta.rateioIds() : SEM_RATEIOS;
    }

    private static String situacao(ConsultaAnalisePedidos consulta) {
        return consulta.situacao() == null ? "" : consulta.situacao().name();
    }

    @Override
    public void removerPorPeriodo(FiltroPedidos filtro) {
        repository.deleteByDataEmissaoPcBetween(filtro.dataInicio(), filtro.dataFim());
    }
}
