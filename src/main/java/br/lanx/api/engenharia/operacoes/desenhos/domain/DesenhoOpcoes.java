package br.lanx.api.engenharia.operacoes.desenhos.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Opções de geração escolhidas no front.
 *
 * @param formatos   A4, painel e/ou esquema elétrico; vazio vira todos
 * @param acabamento acabamento dos trechos sem tubo; {@code null} usa a aba IT TUBOS da lista de corte
 * @param vistas     vistas dos conectores com as cavidades coloridas
 * @param biblioteca usa os DWG da BIBLIOTECA; sem ela não há vistas
 * @param dwg        converte para .dwg; {@code false} entrega só .dxf (mais rápido)
 * @param insumos    gera a planilha de insumos e validações da lista de corte junto no .zip
 */
public record DesenhoOpcoes(
        Set<FormatoDesenho> formatos,
        Acabamento acabamento,
        boolean vistas,
        boolean biblioteca,
        boolean dwg,
        boolean insumos) {

    public DesenhoOpcoes {
        formatos = (formatos == null || formatos.isEmpty())
                ? EnumSet.allOf(FormatoDesenho.class)
                : Set.copyOf(formatos);
        if (!biblioteca)
            vistas = false; // as vistas vêm da BIBLIOTECA
    }

    public static DesenhoOpcoes padrao() {
        return new DesenhoOpcoes(EnumSet.allOf(FormatoDesenho.class), null, true, true, true, true);
    }
}
