package br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Indicadores das não conformidades do período (pela data da NC): totais,
 * taxa sobre as OPs afetadas e as quantidades agrupadas. Grupos em ordem
 * decrescente de quantidade não conforme; meses em ordem cronológica, inclusive
 * os sem registro. {@code dataInicio}/{@code dataFim} ausentes = sem limite.
 */
public record NaoConformidadeIndicadoresResponse(
        LocalDate dataInicio,
        LocalDate dataFim,
        long registros,
        BigDecimal quantidadeNaoConforme,
        long opsAfetadas,
        /** Soma das quantidades das OPs afetadas, cada OP contada uma vez. */
        BigDecimal quantidadeOps,
        /** quantidadeNaoConforme / quantidadeOps em %; ausente sem OPs. */
        BigDecimal percentualNaoConforme,
        List<Grupo> porDepartamento,
        List<Grupo> porTipo,
        List<Grupo> porCliente,
        List<Mes> meses) {

    /** {@code percentual}: fatia da quantidade não conforme total; {@code percentualAcumulado}: Pareto. */
    public record Grupo(
            String nome,
            long registros,
            BigDecimal quantidade,
            BigDecimal percentual,
            BigDecimal percentualAcumulado) {
    }

    /** {@code mes} no formato "2026-09". */
    public record Mes(String mes, long registros, BigDecimal quantidade) {
    }
}
