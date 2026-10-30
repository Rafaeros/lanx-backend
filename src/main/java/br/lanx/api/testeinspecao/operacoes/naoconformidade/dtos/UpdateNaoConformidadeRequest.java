package br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

/** Só os dados da não conformidade: os anexos não mudam na edição. */
public record UpdateNaoConformidadeRequest(
        @NotNull UUID tipoNaoConformidadeId,
        @NotNull UUID departamentoId,
        @NotNull UUID ordemDeProducaoId,
        UUID operadorId,
        @NotNull @Positive BigDecimal quantidadeNaoConforme,
        @NotNull @PastOrPresent LocalDate dataNaoConformidade) {
}
