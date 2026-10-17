package br.lanx.api.compras.relatorios.analisepedidos.application.port.out;

import java.util.UUID;

public interface NormalizeFornecedorPort {

    /** Devolve o id do fornecedor pelo nome, criando-o se preciso; null quando o nome vem vazio. */
    UUID obterOuCriar(String nome, String pais);
}
