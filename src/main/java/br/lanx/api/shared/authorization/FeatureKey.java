package br.lanx.api.shared.authorization;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Identidade estável de uma funcionalidade, em formato de caminho:
 * "compras", "compras.analise", "compras.analise.relatorios".
 * A hierarquia Módulo → Funcionalidade → Subfuncionalidade vem do próprio
 * caminho.
 * Nunca renomeie uma chave publicada: ela é gravada no banco.
 */
public record FeatureKey(String value) {

    private static final int MAX_LENGTH = 100;
    private static final Pattern FORMAT = Pattern.compile("^[a-z][a-z0-9-]{0,29}(\\.[a-z][a-z0-9-]{0,29}){0,3}$");

    public FeatureKey {
        if (value == null || value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
            throw new InvalidFeatureKeyException(
                    "Chave de funcionalidade inválida: '" + value + "' (use minúsculas separadas por ponto)");
        }
    }

    public static FeatureKey of(String value) {
        return new FeatureKey(value);
    }

    /** "compras.analise.relatorios" → "compras" */
    public String module() {
        int dot = value.indexOf('.');
        return dot < 0 ? value : value.substring(0, dot);
    }

    /** "compras.analise.relatorios" → "compras.analise"; "compras" → vazio */
    public Optional<FeatureKey> parent() {
        int dot = value.lastIndexOf('.');
        return dot < 0 ? Optional.empty() : Optional.of(new FeatureKey(value.substring(0, dot)));
    }

    @Override
    public String toString() {
        return value;
    }
}
