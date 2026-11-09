package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.lanx.api.engenharia.operacoes.desenhos.application.event.DesenhoExcluidoEvent;
import br.lanx.api.shared.storage.AnexoStoragePort;

class DesenhoArquivosListenerTest {

    private final List<String> removidos = new ArrayList<>();
    private String chaveQueFalha;

    private final AnexoStoragePort storage = new AnexoStoragePort() {
        public void salvar(String chave, InputStream conteudo) {
            throw new UnsupportedOperationException();
        }

        public InputStream carregar(String chave) {
            throw new UnsupportedOperationException();
        }

        public void remover(String chave) {
            if (chave.equals(chaveQueFalha))
                throw new UncheckedIOException(new java.io.IOException("disco"));
            removidos.add(chave);
        }
    };

    private final DesenhoArquivosListener listener = new DesenhoArquivosListener(storage);

    @Test
    void remove_todos_os_arquivos_do_desenho() {
        listener.aoExcluir(new DesenhoExcluidoEvent(List.of("a", "b")));

        assertThat(removidos).containsExactly("a", "b");
    }

    @Test
    void falha_em_um_arquivo_nao_impede_os_outros() {
        chaveQueFalha = "a";

        listener.aoExcluir(new DesenhoExcluidoEvent(List.of("a", "b")));

        assertThat(removidos).containsExactly("b");
    }
}
