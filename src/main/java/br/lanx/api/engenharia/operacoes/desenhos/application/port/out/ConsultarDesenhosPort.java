package br.lanx.api.engenharia.operacoes.desenhos.application.port.out;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoDetalhe;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoResumo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

/** Leitura: devolve os read models direto do banco, sem montar o agregado. */
public interface ConsultarDesenhosPort {

    /** Mais recentes primeiro; filtros {@code null} são ignorados. */
    PageResult<DesenhoResumo> listar(UUID solicitanteId, StatusDesenho status, String projeto, PageQuery pagina);

    Optional<DesenhoDetalhe> detalhe(UUID jobId);
}
