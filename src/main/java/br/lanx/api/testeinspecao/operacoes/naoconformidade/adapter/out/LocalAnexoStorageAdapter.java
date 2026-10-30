package br.lanx.api.testeinspecao.operacoes.naoconformidade.adapter.out;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import br.lanx.api.testeinspecao.operacoes.naoconformidade.application.port.out.AnexoStoragePort;

@Component
@ConditionalOnProperty(name = "app.storage.tipo", havingValue = "local", matchIfMissing = true)
public class LocalAnexoStorageAdapter implements AnexoStoragePort {

    private final Path diretorio;

    public LocalAnexoStorageAdapter(@Value("${app.storage.base-dir}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public void salvar(String chave, InputStream conteudo) {
        Path destino = resolver(chave);
        try (InputStream input = conteudo) {
            Files.createDirectories(destino.getParent());
            Files.copy(input, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar anexo: " + chave, e);
        }
    }

    @Override
    public InputStream carregar(String chave) {
        try {
            return Files.newInputStream(resolver(chave));
        } catch (IOException e) {
            throw new UncheckedIOException("Anexo não encontrado: " + chave, e);
        }
    }

    @Override
    public void remover(String chave) {
        try {
            Files.deleteIfExists(resolver(chave));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao remover anexo: " + chave, e);
        }
    }

    private Path resolver(String chave) {
        Path caminho = diretorio.resolve(chave).normalize();
        if (!caminho.startsWith(diretorio)) {
            throw new IllegalArgumentException("Chave de armazenamento inválida!");
        }
        return caminho;
    }
}
