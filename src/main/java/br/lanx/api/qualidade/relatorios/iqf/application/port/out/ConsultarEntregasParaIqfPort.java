package br.lanx.api.qualidade.relatorios.iqf.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import br.lanx.api.qualidade.relatorios.iqf.application.dto.EntregaIqfConsulta;
import br.lanx.api.qualidade.relatorios.iqf.application.dto.RateioIqf;

public interface ConsultarEntregasParaIqfPort {

    /** Entregas previstas no período; {@code rateioIds} vazio não filtra. */
    List<EntregaIqfConsulta> consultar(LocalDate inicioPeriodo, LocalDate fimPeriodo, Set<UUID> rateioIds);

    /** Rateios referenciados por algum item importado. */
    List<RateioIqf> listarRateios();

    /** Rateios com esses ids (os inexistentes ficam de fora). */
    List<RateioIqf> buscarRateios(Set<UUID> rateioIds);
}
