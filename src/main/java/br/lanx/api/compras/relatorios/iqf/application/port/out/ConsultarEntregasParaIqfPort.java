package br.lanx.api.compras.relatorios.iqf.application.port.out;

import java.time.LocalDate;
import java.util.List;

import br.lanx.api.compras.relatorios.iqf.application.dto.EntregaIqfConsulta;

public interface ConsultarEntregasParaIqfPort {
    List<EntregaIqfConsulta> consultar(
            LocalDate inicioPeriodo,
            LocalDate fimPeriodo);
}
