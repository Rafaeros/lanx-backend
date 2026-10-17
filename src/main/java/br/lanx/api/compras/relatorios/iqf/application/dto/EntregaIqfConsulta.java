package br.lanx.api.compras.relatorios.iqf.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EntregaIqfConsulta(
        UUID fornecedorId,
        String fornecedor,
        LocalDate dataPrevisaoEntrega,
        LocalDate dataEntrega) {
}
