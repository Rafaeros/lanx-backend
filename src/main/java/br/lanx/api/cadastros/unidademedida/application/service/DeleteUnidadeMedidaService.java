package br.lanx.api.cadastros.unidademedida.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidademedida.application.port.in.DeleteUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaUsageCheck;
import br.lanx.api.cadastros.unidademedida.domain.exceptions.UnidadeMedidaInUseException;
import br.lanx.api.cadastros.unidademedida.domain.exceptions.UnidadeMedidaNotFoundException;

@Service
class DeleteUnidadeMedidaService implements DeleteUnidadeMedidaUseCase {

    private final UnidadeMedidaRepository repository;
    private final List<UnidadeMedidaUsageCheck> usageChecks;

    DeleteUnidadeMedidaService(UnidadeMedidaRepository repository, List<UnidadeMedidaUsageCheck> usageChecks) {
        this.repository = repository;
        this.usageChecks = usageChecks;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new UnidadeMedidaNotFoundException(id);
        }
        if (usageChecks.stream().anyMatch(check -> check.isInUse(id))) {
            throw new UnidadeMedidaInUseException();
        }
        repository.delete(id);
    }
}
