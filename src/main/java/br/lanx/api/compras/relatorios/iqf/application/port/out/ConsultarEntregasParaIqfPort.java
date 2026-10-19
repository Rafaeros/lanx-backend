package br.lanx.api.compras.relatorios.iqf.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import br.lanx.api.compras.relatorios.iqf.application.dto.EntregaIqfConsulta;

public interface ConsultarEntregasParaIqfPort {

    /** Entregas previstas no período; {@code rateios} vazio não filtra. */
    List<EntregaIqfConsulta> consultar(LocalDate inicioPeriodo, LocalDate fimPeriodo, Set<String> rateios);

    List<String> listarRateios();
}
