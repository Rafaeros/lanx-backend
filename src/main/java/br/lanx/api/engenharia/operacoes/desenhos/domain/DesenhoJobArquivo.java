package br.lanx.api.engenharia.operacoes.desenhos.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.shared.domain.Ids;

/**
 * Arquivo de um job no armazenamento compartilhado com o worker ({@code app.storage.base-dir} =
 * {@code ANEXOS_DIRETORIO} do worker).
 *
 * @param chaveArmazenamento caminho relativo à pasta de anexos (ex.: {@code desenhos/<jobId>/<arquivoId>})
 */
public record DesenhoJobArquivo(
        UUID id,
        TipoArquivoDesenho tipo,
        String nomeArquivo,
        String chaveArmazenamento,
        String contentType,
        long tamanhoBytes,
        Instant createdAt) {

    public static final String CONTENT_TYPE_ZIP = "application/zip";

    public DesenhoJobArquivo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(createdAt, "createdAt");
        if (nomeArquivo == null || nomeArquivo.isBlank())
            throw new DesenhoInvalidoException("O nome do arquivo é obrigatório");
        if (chaveArmazenamento == null || chaveArmazenamento.isBlank())
            throw new DesenhoInvalidoException("A chave de armazenamento é obrigatória");
        if (tamanhoBytes <= 0)
            throw new DesenhoInvalidoException("O arquivo \"" + nomeArquivo + "\" está vazio");
        if (contentType == null || contentType.isBlank())
            contentType = "application/octet-stream";
    }

    /** Upload do usuário: a chave é gerada aqui, dentro da pasta do job. */
    static DesenhoJobArquivo enviado(UUID jobId, TipoArquivoDesenho tipo, ArquivoEnviado arquivo, Clock clock) {
        UUID id = Ids.newId();
        return new DesenhoJobArquivo(id, tipo, arquivo.nome(), "desenhos/" + jobId + "/" + id,
                arquivo.contentType(), arquivo.tamanhoBytes(), clock.instant());
    }

    /** Zip gerado pelo worker: a chave é a que veio na mensagem de resultado. */
    public static DesenhoJobArquivo resultado(String chave, String nome, long tamanhoBytes, Clock clock) {
        return new DesenhoJobArquivo(Ids.newId(), TipoArquivoDesenho.RESULTADO_ZIP, nome, chave, CONTENT_TYPE_ZIP,
                tamanhoBytes, clock.instant());
    }
}
