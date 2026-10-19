package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.ObterPedidosExternosPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.FiltroPedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

/**
 * Busca os pedidos no ERP mês a mês: o relatório de um período longo (ex.: 6
 * meses) estoura o tempo de resposta do ERP e a memória do scraper.
 */
@Component
public class AnalisePedidoAdapter implements ObterPedidosExternosPort {

    private final AnalisePedidoClient client;
    private final RawItemPedidoMapper mapper;

    public AnalisePedidoAdapter(AnalisePedidoClient client, RawItemPedidoMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public List<ItemAnalisePedido> obterPedidos(FiltroPedidos filtro) {
        List<ItemAnalisePedido> itens = new ArrayList<>();
        for (FiltroPedidos mes : dividirPorMes(filtro)) {
            client.buscar(mes.dataInicio(), mes.dataFim()).stream().map(mapper::toDomain).forEach(itens::add);
        }
        return itens;
    }

    /** 15/01 a 10/03 → [15/01–31/01, 01/02–28/02, 01/03–10/03]. */
    static List<FiltroPedidos> dividirPorMes(FiltroPedidos filtro) {
        List<FiltroPedidos> partes = new ArrayList<>();
        LocalDate inicio = filtro.dataInicio();
        while (!inicio.isAfter(filtro.dataFim())) {
            LocalDate fimDoMes = inicio.withDayOfMonth(inicio.lengthOfMonth());
            LocalDate fim = fimDoMes.isBefore(filtro.dataFim()) ? fimDoMes : filtro.dataFim();
            partes.add(new FiltroPedidos(inicio, fim));
            inicio = fim.plusDays(1);
        }
        return partes;
    }
}
