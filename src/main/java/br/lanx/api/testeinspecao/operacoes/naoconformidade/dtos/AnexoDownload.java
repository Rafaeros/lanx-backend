package br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos;

import java.io.InputStream;

/** Quem recebe é responsável por fechar o {@code conteudo}. */
public record AnexoDownload(
        String nomeArquivo,
        String contentType,
        long tamanhoBytes,
        InputStream conteudo) {
}
