package br.lanx.api.engenharia.operacoes.desenhos.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;

/** Escrita: carregar o agregado para aplicar uma regra e salvar o resultado. */
public interface DesenhoJobRepository {

    DesenhoJob save(DesenhoJob job);

    Optional<DesenhoJob> findById(UUID id);
}
