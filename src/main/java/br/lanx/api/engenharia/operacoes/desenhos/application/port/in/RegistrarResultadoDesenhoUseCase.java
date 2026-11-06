package br.lanx.api.engenharia.operacoes.desenhos.application.port.in;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;

/** Aplica no job o que o worker avisou (PROCESSANDO, CONCLUIDO ou ERRO). Chamado pelo listener da fila. */
public interface RegistrarResultadoDesenhoUseCase {

    void registrar(ResultadoDesenhoCommand cmd);

    /**
     * @param zip só em CONCLUIDO
     * @param erro só em ERRO: mensagem para o usuário
     */
    record ResultadoDesenhoCommand(UUID jobId, StatusDesenho status, ZipGerado zip, List<String> pendencias,
            String erro) {

        public ResultadoDesenhoCommand {
            Objects.requireNonNull(jobId, "jobId");
            Objects.requireNonNull(status, "status");
            pendencias = pendencias == null ? List.of() : List.copyOf(pendencias);
        }
    }

    record ZipGerado(String chave, String nome, long tamanho) {
    }
}
