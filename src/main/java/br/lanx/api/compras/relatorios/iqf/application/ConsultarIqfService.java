
package br.lanx.api.compras.relatorios.iqf.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.compras.relatorios.iqf.application.dto.EntregaIqfConsulta;
import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfFornecedor;
import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfPeriodo;
import br.lanx.api.compras.relatorios.iqf.application.port.out.ConsultarEntregasParaIqfPort;
import br.lanx.api.compras.relatorios.iqf.domain.AvaliacaoPrazo;
import br.lanx.api.compras.relatorios.iqf.domain.CalculadoraIqfPrazo;

@Service
public class ConsultarIqfService implements ConsultarIqfUseCase {

    private final ConsultarEntregasParaIqfPort consultarEntregasPort;

    public ConsultarIqfService(
            ConsultarEntregasParaIqfPort consultarEntregasPort) {
        this.consultarEntregasPort = consultarEntregasPort;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoIqfPeriodo consultar(
            LocalDate inicioPeriodo,
            LocalDate fimPeriodo) {
        Objects.requireNonNull(inicioPeriodo, "A data inicial é obrigatória.");
        Objects.requireNonNull(fimPeriodo, "A data final é obrigatória.");

        if (inicioPeriodo.isAfter(fimPeriodo)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final.");
        }

        List<EntregaIqfConsulta> entregas = consultarEntregasPort.consultar(inicioPeriodo, fimPeriodo);

        LocalDate dataReferencia = LocalDate.now();

        Map<UUID, ResumoFornecedor> fornecedores = new LinkedHashMap<>();

        long totalAvaliadas = 0;
        long totalNoPrazo = 0;
        long totalAtrasadas = 0;

        for (EntregaIqfConsulta entrega : entregas) {
            if (entrega.fornecedorId() == null) {
                continue;
            }

            ResumoFornecedor resumo = fornecedores.computeIfAbsent(
                    entrega.fornecedorId(),
                    id -> new ResumoFornecedor(id, entrega.fornecedor()));

            AvaliacaoPrazo avaliacao = AvaliacaoPrazo.avaliar(
                    entrega.dataPrevisaoEntrega(),
                    entrega.dataEntrega(),
                    dataReferencia);

            switch (avaliacao) {
                case NO_PRAZO -> {
                    resumo.entregasAvaliadas++;
                    resumo.entregasNoPrazo++;
                    totalAvaliadas++;
                    totalNoPrazo++;
                }
                case ATRASADA -> {
                    resumo.entregasAvaliadas++;
                    resumo.entregasAtrasadas++;
                    totalAvaliadas++;
                    totalAtrasadas++;
                }
                case PENDENTE, NAO_AVALIAVEL -> {
                    // Não entram no denominador do IQF.
                }
            }
        }

        List<ResultadoIqfFornecedor> resultados = new ArrayList<>();

        for (ResumoFornecedor resumo : fornecedores.values()) {
            var percentual = resumo.entregasAvaliadas == 0
                    ? null
                    : CalculadoraIqfPrazo.calcular(
                            resumo.entregasNoPrazo,
                            resumo.entregasAvaliadas).percentual();

            resultados.add(new ResultadoIqfFornecedor(
                    resumo.fornecedorId,
                    resumo.fornecedor,
                    resumo.entregasAvaliadas,
                    resumo.entregasNoPrazo,
                    resumo.entregasAtrasadas,
                    percentual));
        }

        var percentualGeral = totalAvaliadas == 0
                ? null
                : CalculadoraIqfPrazo.calcular(
                        totalNoPrazo,
                        totalAvaliadas).percentual();

        return new ResultadoIqfPeriodo(
                inicioPeriodo,
                fimPeriodo,
                totalAvaliadas,
                totalNoPrazo,
                totalAtrasadas,
                percentualGeral,
                resultados);
    }

    private static final class ResumoFornecedor {

        private final UUID fornecedorId;
        private final String fornecedor;
        private long entregasAvaliadas;
        private long entregasNoPrazo;
        private long entregasAtrasadas;

        private ResumoFornecedor(UUID fornecedorId, String fornecedor) {
            this.fornecedorId = fornecedorId;
            this.fornecedor = fornecedor;
        }
    }
}
