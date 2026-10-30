package br.lanx.api.testeinspecao.operacoes.naoconformidade.application.port.out;

import java.io.InputStream;

/** Falhas de I/O sobem como {@link java.io.UncheckedIOException}. */
public interface AnexoStoragePort {

    void salvar(String chave, InputStream conteudo);

    InputStream carregar(String chave);

    void remover(String chave);
}
