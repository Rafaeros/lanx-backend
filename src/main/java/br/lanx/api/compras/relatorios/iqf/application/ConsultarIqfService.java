
package br.lanx.api.compras.relatorios.iqf.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.compras.relatorios.iqf.application.dto.EntregaIqfConsulta;
import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfFornecedor;
import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfMes;
import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfPeriodo;
import br.lanx.api.compras.relatorios.iqf.application.port.out.ConsultarEntregasParaIqfPort;
import br.lanx.api.compras.relatorios.iqf.domain.AvaliacaoPrazo;
import br.lanx.api.compras.relatorios.iqf.domain.CalculadoraIqfPrazo;
import br.lanx.api.compras.relatorios.iqf.domain.PeriodoIqfInvalidoException;

@Service
public class ConsultarIqfService implements ConsultarIqfUseCase {

    /** Evita que um período absurdo (ex.: 1900-2100) gere milhares de meses vazios. */
    private static final int MAX_MESES = 120;

    private final ConsultarEntregasParaIqfPort consultarEntregasPort;

    public ConsultarIqfService(
            ConsultarEntregasParaIqfPort consultarEntregasPort) {
        this.consultarEntregasPort = consultarEntregasPort;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoIqfPeriodo consultar(
            LocalDate inicioPeriodo,
            LocalDate fimPeriodo,
            Set<String> rateios,
            int toleranciaDias) {
        return calcular(inicioPeriodo, fimPeriodo, rateios, toleranciaDias, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoIqfPeriodo consultarFornecedor(
            UUID fornecedorId,
            LocalDate inicioPeriodo,
            LocalDate fimPeriodo,
            Set<String> rateios,
            int toleranciaDias) {
        if (fornecedorId == null) {
            throw new PeriodoIqfInvalidoException("Informe o fornecedor.");
        }
        return calcular(inicioPeriodo, fimPeriodo, rateios, toleranciaDias, fornecedorId);
    }

    /** {@code fornecedorId} nulo = todos os fornecedores. */
    private ResultadoIqfPeriodo calcular(
            LocalDate inicioPeriodo,
            LocalDate fimPeriodo,
            Set<String> rateios,
            int toleranciaDias,
            UUID fornecedorId) {
        if (toleranciaDias < 0 || toleranciaDias > AvaliacaoPrazo.TOLERANCIA_MAXIMA_DIAS) {
            throw new PeriodoIqfInvalidoException(
                    "A tolerância de atraso deve ficar entre 0 e " + AvaliacaoPrazo.TOLERANCIA_MAXIMA_DIAS + " dias.");
        }
        if (inicioPeriodo == null || fimPeriodo == null) {
            throw new PeriodoIqfInvalidoException("As datas inicial e final são obrigatórias.");
        }

        if (inicioPeriodo.isAfter(fimPeriodo)) {
            throw new PeriodoIqfInvalidoException(
                    "A data inicial não pode ser posterior à data final.");
        }

        YearMonth primeiroMes = YearMonth.from(inicioPeriodo);
        YearMonth ultimoMes = YearMonth.from(fimPeriodo);
        if (primeiroMes.plusMonths(MAX_MESES).isBefore(ultimoMes)) {
            throw new PeriodoIqfInvalidoException(
                    "O período pode ter no máximo " + MAX_MESES / 12 + " anos.");
        }

        Set<String> filtroRateios = normalizar(rateios);
        List<EntregaIqfConsulta> entregas = consultarEntregasPort.consultar(inicioPeriodo, fimPeriodo, filtroRateios);

        LocalDate dataReferencia = LocalDate.now();

        Contagem total = new Contagem();
        Map<UUID, Contagem> porFornecedor = new LinkedHashMap<>();
        Map<UUID, String> nomes = new LinkedHashMap<>();

        // todos os meses do período aparecem, mesmo sem entregas (o gráfico mostra o buraco)
        Map<YearMonth, Contagem> porMes = new LinkedHashMap<>();
        for (YearMonth mes = primeiroMes; !mes.isAfter(ultimoMes); mes = mes.plusMonths(1)) {
            porMes.put(mes, new Contagem());
        }

        for (EntregaIqfConsulta entrega : entregas) {
            if (entrega.fornecedorId() == null
                    || (fornecedorId != null && !fornecedorId.equals(entrega.fornecedorId()))) {
                continue;
            }

            nomes.putIfAbsent(entrega.fornecedorId(), entrega.fornecedor());
            Contagem fornecedor = porFornecedor.computeIfAbsent(entrega.fornecedorId(), id -> new Contagem());

            AvaliacaoPrazo avaliacao = AvaliacaoPrazo.avaliar(
                    entrega.dataPrevisaoEntrega(),
                    entrega.dataEntrega(),
                    dataReferencia,
                    toleranciaDias);

            total.registrar(avaliacao);
            fornecedor.registrar(avaliacao);
            if (entrega.dataPrevisaoEntrega() != null) {
                Contagem mes = porMes.get(YearMonth.from(entrega.dataPrevisaoEntrega()));
                if (mes != null) {
                    mes.registrar(avaliacao);
                }
            }
        }

        List<ResultadoIqfFornecedor> fornecedores = new ArrayList<>();
        porFornecedor.forEach((id, c) -> fornecedores.add(new ResultadoIqfFornecedor(
                id, nomes.get(id), c.avaliadas, c.noPrazo, c.atrasadas, c.percentual())));

        List<ResultadoIqfMes> meses = new ArrayList<>();
        porMes.forEach((mes, c) -> meses.add(new ResultadoIqfMes(
                mes, c.avaliadas, c.noPrazo, c.atrasadas, c.percentual())));

        return new ResultadoIqfPeriodo(
                inicioPeriodo,
                fimPeriodo,
                filtroRateios,
                toleranciaDias,
                total.avaliadas,
                total.noPrazo,
                total.atrasadas,
                total.percentual(),
                fornecedores,
                meses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarRateios() {
        return consultarEntregasPort.listarRateios();
    }

    private static Set<String> normalizar(Set<String> rateios) {
        if (rateios == null) {
            return Set.of();
        }
        return rateios.stream()
                .filter(r -> r != null && !r.isBlank())
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Entregas avaliadas de um recorte (total, fornecedor ou mês). */
    private static final class Contagem {

        private long avaliadas;
        private long noPrazo;
        private long atrasadas;

        void registrar(AvaliacaoPrazo avaliacao) {
            switch (avaliacao) {
                case NO_PRAZO -> {
                    avaliadas++;
                    noPrazo++;
                }
                case ATRASADA -> {
                    avaliadas++;
                    atrasadas++;
                }
                case PENDENTE, NAO_AVALIAVEL -> {
                    // Não entram no denominador do IQF.
                }
            }
        }

        BigDecimal percentual() {
            return avaliadas == 0 ? null : CalculadoraIqfPrazo.calcular(noPrazo, avaliadas).percentual();
        }
    }
}
