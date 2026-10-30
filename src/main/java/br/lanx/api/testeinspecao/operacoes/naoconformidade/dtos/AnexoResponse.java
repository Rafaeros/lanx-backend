package br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos;

import java.time.Instant;
import java.util.UUID;

import br.lanx.api.testeinspecao.operacoes.naoconformidade.NaoConformidadeAnexo;

/** Sem a chave de armazenamento: o download é feito pelo id do anexo. */
public record AnexoResponse(
        UUID id,
        String nomeArquivo,
        String contentType,
        Long tamanhoBytes,
        Instant createdAt) {
    public static AnexoResponse fromEntity(NaoConformidadeAnexo a) {
        return new AnexoResponse(a.getId(), a.getNomeArquivo(), a.getContentType(), a.getTamanhoBytes(),
                a.getCreatedAt());
    }
}
