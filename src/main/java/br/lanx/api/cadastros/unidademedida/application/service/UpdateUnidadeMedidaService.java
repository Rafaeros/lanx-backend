package br.lanx.api.cadastros.unidademedida.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidademedida.application.port.in.UpdateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;
import br.lanx.api.cadastros.unidademedida.domain.exceptions.UnidadeMedidaAlreadyExistsException;
import br.lanx.api.cadastros.unidademedida.domain.exceptions.UnidadeMedidaNotFoundException;

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
                .orElseThrow(() -> new UnidadeMedidaNotFoundException(cmd.id()));

        UnidadeMedida atualizada = unidade.update(cmd.novoNome(), cmd.novaSigla());

        boolean siglaMudou = !atualizada.sigla().equals(unidade.sigla());
        if (siglaMudou && unidades.existsBySigla(atualizada.sigla())) {
            throw new UnidadeMedidaAlreadyExistsException("Já existe uma unidade de medida com essa Sigla");
        }
        unidades.save(atualizada);
    }
}
