package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.persistence;

import java.util.List;

/**
 * Formato da coluna JSONB {@code opcoes}:
 * {@code {"formatos":["a4","painel"],"acabamento":"F","vistas":true,"biblioteca":true,"dwg":true}}.
 *
 * <p>Separado de {@code DesenhoOpcoes} de propósito: renomear algo no domínio não pode mudar o JSON já
 * gravado nas linhas antigas. Só tipos simples (String/boolean), convertidos pelo mapper.
 */
public record OpcoesJson(List<String> formatos, String acabamento, boolean vistas, boolean biblioteca, boolean dwg) {
}
