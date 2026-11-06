package br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte;

import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * Lê a quantidade de vias do código LANX do conector.
 *
 * <p>Padrão: {@code CN} + {@code F}/{@code M} (fêmea/macho) + letras da família + 2 dígitos de vias + o
 * resto (cor, variante). Exemplos: {@code CNFAT04CZ} = fêmea Amphenol 04 vias cinza; {@code CNMAT06CZ},
 * {@code CNFDT06BG}, {@code CNFDTE06PRCE} = 06 vias.
 *
 * <p>Código fora do padrão devolve vazio: a análise simplesmente não confere as vias desse conector.
 */
public final class CodigoConector {

    private static final Pattern PADRAO = Pattern.compile("^CN[FM][A-Z]{1,5}(\\d{2})(?!\\d)");

    private CodigoConector() {
    }

    public static OptionalInt vias(String codigo) {
        if (codigo == null)
            return OptionalInt.empty();
        var m = PADRAO.matcher(codigo.trim().toUpperCase());
        if (!m.find())
            return OptionalInt.empty();
        int vias = Integer.parseInt(m.group(1));
        return vias > 0 ? OptionalInt.of(vias) : OptionalInt.empty();
    }
}
