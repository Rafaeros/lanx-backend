package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.compradores.CompradorUsageCheck;

/** Itens importados vetam a exclusão de um comprador que ainda referenciam. */
@Component
class AnalisePedidoCompradorUsageCheck implements CompradorUsageCheck {

    private final AnalisePedidoRepository repository;

    AnalisePedidoCompradorUsageCheck(AnalisePedidoRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isInUse(UUID compradorId) {
        return repository.existsByCompradorId(compradorId);
    }
}
