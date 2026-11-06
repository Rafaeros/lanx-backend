package br.lanx.api.engenharia.operacoes.desenhos.application.port.in;

import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.ArquivoDesenhoDownload;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoDetalhe;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoResumo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

/** Leituras: histórico, detalhe e download. Não mudam nada (lado "query"). */
public interface ConsultarDesenhoUseCase {

    PageResult<DesenhoResumo> listar(ListarDesenhosQuery query);

    DesenhoDetalhe buscar(UUID jobId);

    /** Abre um arquivo do job (lista de corte, PDF do cliente ou o zip gerado). */
    ArquivoDesenhoDownload baixarArquivo(UUID jobId, UUID arquivoId);

    /**
     * @param solicitanteId só os pedidos dessa pessoa; {@code null} = todos
     * @param projeto       trecho do código do projeto; {@code null}/vazio = todos
     * @param status        {@code null} = todos
     */
    record ListarDesenhosQuery(UUID solicitanteId, String projeto, StatusDesenho status, PageQuery pagina) {
    }
}
