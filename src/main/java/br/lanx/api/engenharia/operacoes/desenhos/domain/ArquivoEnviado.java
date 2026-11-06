package br.lanx.api.engenharia.operacoes.desenhos.domain;

/**
 * Dados de um upload antes de virar {@link DesenhoJobArquivo} (que precisa do id do job para a chave).
 * Os bytes não passam pelo domínio: o service grava no storage usando a chave gerada.
 */
public record ArquivoEnviado(String nome, String contentType, long tamanhoBytes) {
}
