package br.lanx.api.engenharia.operacoes.desenhos.domain;

import java.util.Set;

/**
 * Opções de geração escolhidas no front.
 *
 * @param formatos   A4, painel ou os dois; vazio vira só A4
 * @param acabamento acabamento dos trechos sem tubo; {@code null} usa a aba IT TUBOS da lista de corte
 * @param vistas     vistas dos conectores com as cavidades coloridas
 * @param biblioteca usa os DWG da BIBLIOTECA; sem ela não há vistas
 * @param dwg        converte para .dwg; {@code false} entrega só .dxf (mais rápido)
 */
public record DesenhoOpcoes(
        Set<FormatoDesenho> formatos,
        Acabamento acabamento,
        boolean vistas,
        boolean biblioteca,
        boolean dwg) {

    public DesenhoOpcoes {
        formatos = (formatos == null || formatos.isEmpty()) ? Set.of(FormatoDesenho.A4) : Set.copyOf(formatos);
        if (!biblioteca)
            vistas = false; // as vistas vêm da BIBLIOTECA
    }

    public static DesenhoOpcoes padrao() {
        return new DesenhoOpcoes(Set.of(FormatoDesenho.A4), null, true, true, true);
    }
}
