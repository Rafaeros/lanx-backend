package br.lanx.api.compras.relatorios.iqf.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence.AnalisePedidoRepository;
import br.lanx.api.compras.relatorios.iqf.application.dto.EntregaIqfConsulta;
import br.lanx.api.compras.relatorios.iqf.application.port.out.ConsultarEntregasParaIqfPort;

@Component
public class ConsultarEntregasParaIqfAdapter implements ConsultarEntregasParaIqfPort {

    private final AnalisePedidoRepository repository;

    public ConsultarEntregasParaIqfAdapter(AnalisePedidoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<EntregaIqfConsulta> consultar(LocalDate inicioPeriodo, LocalDate fimPeriodo) {
        return repository.buscarEntregasParaIqf(inicioPeriodo, fimPeriodo)
                .stream()
                .map(item -> new EntregaIqfConsulta(
                        item.getFornecedor() == null ? null : item.getFornecedor().getId(),
                        item.getFornecedor() == null ? null : item.getFornecedor().getNome(),
                        item.getDataPrevisaoEntregaPc(),
                        item.getDataEntregaPc()))
                .toList();
    }

}
