package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.scheduling;

import java.time.Clock;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.in.ImportarAnalisePedidosUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;

@Component
class ImportacaoAgendadaAnalisePedidos {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoAgendadaAnalisePedidos.class);

    private final ImportarAnalisePedidosUseCase importarAnalisePedidosUseCase;
    private final ImportacaoAgendadaProperties properties;
    private final Clock clock;

    public ImportacaoAgendadaAnalisePedidos(ImportarAnalisePedidosUseCase importarAnalisePedidosUseCase,
            ImportacaoAgendadaProperties properties, Clock clock) {
        this.importarAnalisePedidosUseCase = importarAnalisePedidosUseCase;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "${app.compras.analse-pedidos.importacao-agendada.cron:-}", zone = "America/Sao_Paulo")
    void importar() {
        LocalDate hoje = LocalDate.now(clock);
        var filtro = new FiltroPedidos(hoje.minusDays(properties.janela().toDays()), hoje);
        try {
            int itens = importarAnalisePedidosUseCase.importar(filtro);
            log.info("Importação agendada da análise de pedidos ({} a {}): {} itens",
                    filtro.dataInicio(), filtro.dataFim(), itens);
        } catch (RuntimeException e) {
            // cg-scraper fora do ar, timeout...: tenta de novo no próximo horário
            log.error("Falha na importação agendada da análise de pedidos", e);
        }
    }
}
