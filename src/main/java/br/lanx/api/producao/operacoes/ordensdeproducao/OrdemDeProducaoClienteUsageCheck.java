package br.lanx.api.producao.operacoes.ordensdeproducao;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.clientes.ClienteUsageCheck;

/** Ordens de produção vetam a exclusão de um cliente que ainda referenciam. */
@Component
class OrdemDeProducaoClienteUsageCheck implements ClienteUsageCheck {

    private final OrdemDeProducaoRepository repository;

    OrdemDeProducaoClienteUsageCheck(OrdemDeProducaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isInUse(UUID clienteId) {
        return repository.existsByClienteId(clienteId);
    }
}
