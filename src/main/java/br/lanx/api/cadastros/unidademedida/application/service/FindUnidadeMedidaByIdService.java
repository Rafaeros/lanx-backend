package br.lanx.api.cadastros.unidademedida.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidademedida.application.port.in.FindUnidadeMedidaByIdUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;

@Service
public class FindUnidadeMedidaByIdService implements FindUnidadeMedidaByIdUseCase {

    private final UnidadeMedidaRepository unidades;

    public FindUnidadeMedidaByIdService(UnidadeMedidaRepository unidades) {
        this.unidades = unidades;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UnidadeMedida> findById(UUID id) {
        return unidades.findById(id);
    }

}