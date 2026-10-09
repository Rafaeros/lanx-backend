package br.lanx.api.cadastros.unidade_medida.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidade_medida.application.port.in.DeleteUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidade_medida.application.port.out.UnidadeMedidaRepository;

@Service
class DeleteUnidadeMedidaService implements DeleteUnidadeMedidaUseCase {
    private final UnidadeMedidaRepository repository;

    DeleteUnidadeMedidaService(UnidadeMedidaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(id);
    }
}
