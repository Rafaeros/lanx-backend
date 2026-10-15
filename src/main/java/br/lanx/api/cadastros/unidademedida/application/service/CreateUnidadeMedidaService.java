package br.lanx.api.cadastros.unidademedida.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidademedida.application.port.in.CreateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;
import br.lanx.api.cadastros.unidademedida.domain.exceptions.UnidadeMedidaAlreadyExistsException;
import br.lanx.api.shared.application.CreatedResult;

@Service
public class CreateUnidadeMedidaService implements CreateUnidadeMedidaUseCase {

    private final UnidadeMedidaRepository unidades;

    public CreateUnidadeMedidaService(UnidadeMedidaRepository unidades) {
        this.unidades = unidades;
    }

    @Override
    @Transactional
    public CreatedResult create(CreateUnidadeMedidaCommand cmd) {
        if (unidades.existsBySigla(cmd.sigla())) {
            throw new UnidadeMedidaAlreadyExistsException("Já existe uma unidade de medida com essa Sigla");
        }

        UnidadeMedida unidade = UnidadeMedida.create(cmd.nome(), cmd.sigla());
        return new CreatedResult(unidades.save(unidade).id());
    }

}
