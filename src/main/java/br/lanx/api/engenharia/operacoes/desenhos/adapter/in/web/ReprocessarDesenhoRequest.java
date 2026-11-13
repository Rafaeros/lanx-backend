package br.lanx.api.engenharia.operacoes.desenhos.adapter.in.web;

import java.util.List;

import jakarta.validation.constraints.Pattern;

/**
 * Corpo (opcional) do "Reprocessar": as opções marcadas na tela do desenho. Sem corpo, gera de novo com as
 * opções do pedido. Campos nulos seguem o padrão do pedido novo (tudo ligado; formatos vazio = todos).
 */
public record ReprocessarDesenhoRequest(
        List<String> formatos,
        @Pattern(regexp = "^[A-JLM]$", message = "Acabamento inválido (use A–J, L ou M)") String acabamento,
        Boolean vistas,
        Boolean biblioteca,
        Boolean dwg,
        Boolean insumos) {
}
