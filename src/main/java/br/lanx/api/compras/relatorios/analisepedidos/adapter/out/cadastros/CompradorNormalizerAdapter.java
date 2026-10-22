package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.cadastros;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.compradores.CompradorService;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeCompradorPort;

@Component
public class CompradorNormalizerAdapter implements NormalizeCompradorPort {

    private final CompradorService compradorService;

    public CompradorNormalizerAdapter(CompradorService compradorService) {
        this.compradorService = compradorService;
    }

    @Override
    public UUID obterOuCriar(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return compradorService.findByNomeOrCreate(nome).getId();
    }
}
