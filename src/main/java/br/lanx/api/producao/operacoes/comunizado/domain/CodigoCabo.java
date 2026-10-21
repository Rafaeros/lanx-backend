package br.lanx.api.producao.operacoes.comunizado.domain;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Código do cabo decomposto: MODELO (3 letras) + BITOLA (4 dígitos) + COR +
 * COR DA FAIXA (opcional) + SUFIXO (opcional).
 * <ul>
 * <li>CFR0100AM → CFR, 0100 (1,0 mm²), amarelo</li>
 * <li>CFR0035AZPR → CFR, 0035, azul com faixa preta</li>
 * <li>CFR0100AZCLBR → CFR, 0100, azul claro com faixa branca</li>
 * <li>CFR0050VDSEV → CFR, 0050, verde, sufixo SEV</li>
 * </ul>
 */
public record CodigoCabo(String modelo, int bitola, CorCabo cor, CorCabo faixa, String sufixo) {

    private static final Pattern FORMATO = Pattern.compile("^([A-Z]{3})(\\d{4})([A-Z]*)$");

    /** Ordem do comunizado: modelo, bitola (menor primeiro), cor e faixa na ordem do mapeamento. */
    public static final Comparator<CodigoCabo> ORDEM = Comparator
            .comparing(CodigoCabo::modelo)
            .thenComparingInt(CodigoCabo::bitola)
            .thenComparingInt(c -> c.cor().ordem())
            .thenComparingInt(c -> c.faixa() == null ? -1 : c.faixa().ordem())
            .thenComparing(CodigoCabo::sufixo);

    /** Vazio quando o tipo não segue o padrão ou a cor não está no mapeamento. */
    public static Optional<CodigoCabo> ler(String tipo, CoresCabos cores) {
        if (tipo == null) {
            return Optional.empty();
        }
        Matcher m = FORMATO.matcher(tipo.trim().toUpperCase(Locale.ROOT));
        if (!m.matches()) {
            return Optional.empty();
        }
        String resto = m.group(3);
        Optional<CorCabo> cor = cores.prefixo(resto);
        if (cor.isEmpty()) {
            return Optional.empty();
        }
        resto = resto.substring(cor.get().codigo().length());
        CorCabo faixa = cores.prefixo(resto).orElse(null);
        if (faixa != null) {
            resto = resto.substring(faixa.codigo().length());
        }
        return Optional.of(new CodigoCabo(m.group(1), Integer.parseInt(m.group(2)), cor.get(), faixa, resto));
    }

    /** "CFR0035AZPR" → "CFR0035AZ": tudo antes da sigla da faixa (para colorir a faixa à parte). */
    public int inicioDaFaixa() {
        return 3 + 4 + cor.codigo().length();
    }
}
