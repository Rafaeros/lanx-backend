package br.lanx.api.engenharia.operacoes.desenhos.application.dto;

import java.io.InputStream;

/** Arquivo pronto para o download; quem recebe fecha o stream (o Spring fecha ao terminar a resposta). */
public record ArquivoDesenhoDownload(String nomeArquivo, String contentType, long tamanhoBytes,
        InputStream conteudo) {
}
