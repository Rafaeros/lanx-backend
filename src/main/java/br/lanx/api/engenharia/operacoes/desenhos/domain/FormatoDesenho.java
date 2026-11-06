package br.lanx.api.engenharia.operacoes.desenhos.domain;

import java.util.Arrays;

import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;

/** Formato de saída do desenho; {@link #flag()} é o texto usado pelo worker, pelo front e no JSONB. */
public enum FormatoDesenho {
    A4("a4"), PAINEL("painel");

    private final String flag;

    FormatoDesenho(String flag) {
        this.flag = flag;
    }

    public String flag() {
        return flag;
    }

    /** {@code "a4"} → {@link #A4}; texto desconhecido é dado inválido de quem chamou. */
    public static FormatoDesenho deFlag(String flag) {
        return Arrays.stream(values())
                .filter(f -> f.flag.equalsIgnoreCase(flag))
                .findFirst()
                .orElseThrow(() -> new DesenhoInvalidoException("Formato de desenho desconhecido: " + flag));
    }
}
