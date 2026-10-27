package br.lanx.api.qualidade.relatorios.iqf.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.rateios.RateioRepository;
import br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence.AnalisePedidoEntity;
import br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence.AnalisePedidoRepository;
import br.lanx.api.qualidade.relatorios.iqf.application.dto.EntregaIqfConsulta;
import br.lanx.api.qualidade.relatorios.iqf.application.dto.RateioIqf;
import br.lanx.api.qualidade.relatorios.iqf.application.port.out.ConsultarEntregasParaIqfPort;

@Component
public class ConsultarEntregasParaIqfAdapter implements ConsultarEntregasParaIqfPort {

    private final AnalisePedidoRepository repository;
    private final RateioRepository rateioRepository;

    public ConsultarEntregasParaIqfAdapter(AnalisePedidoRepository repository, RateioRepository rateioRepository) {
        this.repository = repository;
        this.rateioRepository = rateioRepository;
    }

    @Override
    public List<EntregaIqfConsulta> consultar(LocalDate inicioPeriodo, LocalDate fimPeriodo, Set<UUID> rateioIds) {
        // "in ()" vazio não é SQL válido: sem rateios, usa a consulta sem o filtro
        List<AnalisePedidoEntity> itens = rateioIds.isEmpty()
                ? repository.buscarEntregasParaIqf(inicioPeriodo, fimPeriodo)
                : repository.buscarEntregasParaIqfPorRateio(inicioPeriodo, fimPeriodo, rateioIds);
        return itens.stream()
                .map(item -> new EntregaIqfConsulta(
                        item.getFornecedor() == null ? null : item.getFornecedor().getId(),
                        item.getFornecedor() == null ? null : item.getFornecedor().getNome(),
                        item.getDataPrevisaoEntregaPc(),
                        item.getDataEntregaPc()))
                .toList();
    }

    @Override
    public List<RateioIqf> listarRateios() {
        return repository.listarRateios().stream().map(r -> new RateioIqf(r.getId(), r.getNome())).toList();
    }

    @Override
    public List<RateioIqf> buscarRateios(Set<UUID> rateioIds) {
        return rateioRepository.findAllById(rateioIds).stream().map(r -> new RateioIqf(r.getId(), r.getNome())).toList();
    }
}
