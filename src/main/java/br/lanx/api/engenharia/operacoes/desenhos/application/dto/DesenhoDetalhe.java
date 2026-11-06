package br.lanx.api.engenharia.operacoes.desenhos.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoOpcoes;
import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;
import br.lanx.api.engenharia.operacoes.desenhos.domain.TipoArquivoDesenho;

public record DesenhoDetalhe(
        UUID id,
        UUID solicitanteId,
        String projeto,
        StatusDesenho status,
        DesenhoOpcoes opcoes,
        List<String> pendencias,
        String erro,
        int tentativas,
        List<Arquivo> arquivos,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt) {
    public record Arquivo(UUID id, TipoArquivoDesenho tipo, String nome, long tamanhoBytes) {
    }
}
