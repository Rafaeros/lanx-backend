package br.lanx.api.cadastros.unidade_medida.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidade_medida.application.port.in.ExistsUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidade_medida.application.port.out.UnidadeMedidaRepository;

@Service 
public class ExistsUnidadeMedidaService implements ExistsUnidadeMedidaUseCase {
    
    private final UnidadeMedidaRepository unidades;

    public ExistsUnidadeMedidaService(UnidadeMedidaRepository unidades) {
        this.unidades = unidades;
    }

    @Override 
    @Transactional (readOnly = true)
    public boolean existsById(UUID id) {
        return unidades.existsById(id);
    }

}
