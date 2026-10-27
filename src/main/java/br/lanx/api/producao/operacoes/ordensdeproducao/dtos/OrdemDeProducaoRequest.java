package br.lanx.api.producao.operacoes.ordensdeproducao.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Corpo de POST e PUT em /producao/operacoes/ordens-de-producao. {@code etapaAtual} é opcional. */
public record OrdemDeProducaoRequest(
        @NotBlank(message = "O código da OP é obrigatório") @Size(max = 10, message = "Máximo de 10 caracteres") String codigo,
        @NotNull(message = "O cliente é obrigatório") UUID clienteId,
        @NotNull(message = "O material é obrigatório") UUID materialId,
        @NotNull(message = "A quantidade é obrigatória") @Positive(message = "A quantidade deve ser maior que zero")
        @Digits(integer = 15, fraction = 4) BigDecimal quantidade,
        @NotNull(message = "A previsão é obrigatória") LocalDate previsao,
        @Size(max = 100) String etapaAtual) {
}
