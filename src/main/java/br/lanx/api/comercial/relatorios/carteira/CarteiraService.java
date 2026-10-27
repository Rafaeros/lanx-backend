package br.lanx.api.comercial.relatorios.carteira;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.lanx.api.comercial.relatorios.carteira.dtos.CarteiraResponse;
import br.lanx.api.comercial.relatorios.carteira.erp.CarteiraErpClient;
import br.lanx.api.comercial.relatorios.carteira.erp.CarteiraLinhaErp;
import br.lanx.api.comercial.relatorios.carteira.exceptions.PeriodoCarteiraInvalidoException;

/**
 * Carteira de pedidos vinda do ERP. Cada consulta ao ERP é lenta (~1,5 min por
 * ano), então o resultado fica em memória por {@link #VALIDADE} por período:
 * exportar a planilha logo depois de consultar não consulta o ERP de novo.
 */
@Service
public class CarteiraService {

    static final Duration VALIDADE = Duration.ofMinutes(10);

    private record Periodo(LocalDate inicio, LocalDate fim) {
    }

    private record Consulta(Instant em, List<CarteiraLinhaErp> linhas) {
    }

    private static final Comparator<CarteiraResponse.Item> POR_PREVISAO = Comparator
            .comparing(CarteiraResponse.Item::previsao, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(CarteiraResponse.Item::negociacao, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(CarteiraResponse.Item::ordem, Comparator.nullsLast(Comparator.naturalOrder()));

    private final CarteiraErpClient erp;
    private final Clock clock;
    private final Map<Periodo, Consulta> cache = new ConcurrentHashMap<>();

    @Autowired
    public CarteiraService(CarteiraErpClient erp) {
        this(erp, Clock.systemDefaultZone());
    }

    CarteiraService(CarteiraErpClient erp, Clock clock) {
        this.erp = erp;
        this.clock = clock;
    }

    /** {@code atualizar}: ignora o cache e consulta o ERP de novo. */
    public CarteiraResponse consultar(LocalDate inicio, LocalDate fim, boolean atualizar) {
        if (fim.isBefore(inicio)) {
            throw new PeriodoCarteiraInvalidoException("A data inicial precisa ser anterior ou igual à final.");
        }
        var periodo = new Periodo(inicio, fim);
        Consulta consulta = atualizar ? null : cache.get(periodo);
        if (consulta == null || expirada(consulta)) {
            consulta = new Consulta(clock.instant(), List.copyOf(erp.buscar(inicio, fim)));
            cache.values().removeIf(this::expirada);
            cache.put(periodo, consulta);
        }
        return montar(periodo, consulta);
    }

    private boolean expirada(Consulta consulta) {
        return consulta.em().plus(VALIDADE).isBefore(clock.instant());
    }

    private CarteiraResponse montar(Periodo periodo, Consulta consulta) {
        LocalDate hoje = LocalDate.now(clock);
        List<CarteiraResponse.Item> itens = consulta.linhas().stream()
                .filter(Objects::nonNull)
                .map(linha -> item(linha, hoje))
                .sorted(POR_PREVISAO)
                .toList();

        var resumo = new CarteiraResponse.Resumo(
                itens.size(),
                itens.stream().map(CarteiraResponse.Item::valorTotal).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                (int) itens.stream().filter(CarteiraResponse.Item::atrasado).count(),
                (int) itens.stream().filter(i -> !i.materiaisPendentes().isEmpty()).count());

        return new CarteiraResponse(periodo.inicio(), periodo.fim(), consulta.em(), resumo, itens);
    }

    private static CarteiraResponse.Item item(CarteiraLinhaErp linha, LocalDate hoje) {
        List<CarteiraResponse.MaterialPendente> materiais = linha.materiaisPendentes() == null ? List.of()
                : linha.materiaisPendentes().stream()
                        .filter(Objects::nonNull)
                        .map(m -> new CarteiraResponse.MaterialPendente(m.codigo(), m.descricao(),
                                m.quantidadePendente(), m.unidadeMedida(), m.previsao()))
                        .toList();
        return new CarteiraResponse.Item(
                linha.negociacao(),
                linha.pedidoCliente(),
                linha.ordem(),
                linha.tipoServico(),
                linha.cliente(),
                linha.codigo(),
                linha.descricao(),
                linha.previsao(),
                linha.pendente(),
                linha.valorTotal(),
                linha.etapaAtual(),
                linha.previsao() != null && linha.previsao().isBefore(hoje),
                materiais);
    }
}
