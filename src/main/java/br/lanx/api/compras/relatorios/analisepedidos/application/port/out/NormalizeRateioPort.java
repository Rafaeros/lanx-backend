package br.lanx.api.compras.relatorios.analisepedidos.application.port.out;

import java.util.UUID;

public interface NormalizeRateioPort {

    /** Devolve o id do rateio pelo nome, criando-o se preciso; null quando o nome vem vazio. */
    UUID obterOuCriar(String nome);
}
