package br.lanx.api.producao.operacoes.ordensdeproducao.dtos;

/** Linha da planilha que não entrou, com o motivo ({@code linha} = número da linha no Excel). */
public record LinhaRejeitada(int linha, String codigo, String motivo) {
}
