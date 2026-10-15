package br.lanx.api.cadastros.unidademedida.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.unidademedida.application.port.in.FindOrCreateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;
import br.lanx.api.cadastros.unidademedida.domain.exceptions.UnidadeMedidaAlreadyExistsException;

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

        // 1. Buscar pela sigla primeiro (identificador funcional)
        Optional<UnidadeMedida> existentePorSigla = unidades.findBySigla(nova.sigla());
        if (existentePorSigla.isPresent()) {
            return new Result(existentePorSigla.get(), false);
        }

        // 2. Se não achou por sigla, buscar por nome (caso tenha sido criado
        // manualmente)
        Optional<UnidadeMedida> existentePorNome = unidades.findByNome(nova.nome());
        if (existentePorNome.isPresent()) {
            return new Result(existentePorNome.get(), false);
        }

        return new Result(unidades.save(nova), true);
    }
}
