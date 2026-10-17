package br.lanx.api.compras.relatorios.iqf.application;

import java.time.LocalDate;

import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfPeriodo;

public interface ConsultarIqfUseCase {
    ResultadoIqfPeriodo consultar(LocalDate inicioPeriodo,
            LocalDate fimPeriodo);
}
