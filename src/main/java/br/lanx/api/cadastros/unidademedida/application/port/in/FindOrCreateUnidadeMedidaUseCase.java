package br.lanx.api.cadastros.unidademedida.application.port.in;

import br.lanx.api.cadastros.unidademedida.domain.UnidadeMedida;

/**
 * Devolve a unidade com esse nome (sem diferenciar maiúsculas) ou cria uma nova
 * com a sigla informada. Serve a importações e cadastros que citam a unidade
 * pelo nome.
 */
public interface FindOrCreateUnidadeMedidaUseCase {
    Result findByNomeOrCreate(Command command);

    record Command(String nome, String sigla) {
    }

    /** {@code created} diz se a unidade acabou de ser criada ou já existia. */
    record Result(UnidadeMedida unidade, boolean created) {
    }
}
