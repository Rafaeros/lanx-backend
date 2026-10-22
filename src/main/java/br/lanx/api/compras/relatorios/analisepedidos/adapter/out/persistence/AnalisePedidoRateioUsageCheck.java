package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.rateios.RateioUsageCheck;

/** Itens importados vetam a exclusão de um rateio que ainda referenciam. */
@Component
class AnalisePedidoRateioUsageCheck implements RateioUsageCheck {

    private final AnalisePedidoRepository repository;

    AnalisePedidoRateioUsageCheck(AnalisePedidoRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isInUse(UUID rateioId) {
        return repository.existsByRateioId(rateioId);
    }
}
