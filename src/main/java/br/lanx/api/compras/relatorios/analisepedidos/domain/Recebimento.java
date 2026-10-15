package br.lanx.api.compras.relatorios.analisepedidos.domain;

public record Recebimento(
        String numero,
        String notaFiscal,
        String invoice,
        Integer quantidadeRecebimentos) {

}
