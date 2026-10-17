package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.cadastros;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.fornecedores.FornecedorService;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeFornecedorPort;

@Component
public class FornecedorNormalizerAdapter implements NormalizeFornecedorPort {

    private final FornecedorService fornecedorService;

    public FornecedorNormalizerAdapter(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @Override
    public UUID obterOuCriar(String nome, String pais) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return fornecedorService.findByNomeOrCreate(nome.trim(), pais).getId();
    }

}
