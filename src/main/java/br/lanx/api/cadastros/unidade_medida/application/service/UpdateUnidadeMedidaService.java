package br.lanx.api.cadastros.unidade_medida.application.service;

import org.springframework.stereotype.Service;

import br.lanx.api.cadastros.unidade_medida.application.port.in.UpdateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidade_medida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UpdateUnidadeMedidaService implements UpdateUnidadeMedidaUseCase {

    private final UnidadeMedidaRepository unidades;

    UpdateUnidadeMedidaService(UnidadeMedidaRepository unidades) {
        this.unidades = unidades;
    }

    @Override
    @Transactional
    public void update(UpdateUnidadeMedidaCommand cmd) {
        UnidadeMedida unidade = unidades.findById(cmd.id())
                .orElseThrow(() -> new EntityNotFoundException("Não encontrada"));

        UnidadeMedida unidadeAtualiada = unidade.update(
                cmd.novoNome(),
                cmd.novaSigla());

        unidades.save(unidadeAtualiada);
    }

}
