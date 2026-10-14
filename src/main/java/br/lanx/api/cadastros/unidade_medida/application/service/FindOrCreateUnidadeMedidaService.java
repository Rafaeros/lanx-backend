package br.lanx.api.cadastros.unidade_medida.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidade_medida.application.port.in.FindOrCreateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidade_medida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;
import br.lanx.api.cadastros.unidade_medida.domain.exceptions.UnidadeMedidaAlreadyExistsException;

@Service
public class FindOrCreateUnidadeMedidaService implements FindOrCreateUnidadeMedidaUseCase {

    private final UnidadeMedidaRepository unidades;

    FindOrCreateUnidadeMedidaService(UnidadeMedidaRepository unidades) {
        this.unidades = unidades;
    }

    @Override
    @Transactional
    public Result findByNomeOrCreate(Command cmd) {
        UnidadeMedida nova = UnidadeMedida.create(cmd.nome(), cmd.sigla());

        var existente = unidades.findByNome(nova.nome());
        if (existente.isPresent()) {
            return new Result(existente.get(), false);
        }

        // nome novo, mas a sigla pertence a outra unidade: não dá para criar
        if (unidades.existsBySigla(nova.sigla())) {
            throw new UnidadeMedidaAlreadyExistsException("Já existe uma unidade de medida com essa Sigla");
        }
        return new Result(unidades.save(nova), true);
    }
}
