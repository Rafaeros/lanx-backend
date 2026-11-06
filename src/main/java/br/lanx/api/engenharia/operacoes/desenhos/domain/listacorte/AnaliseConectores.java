package br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.ProblemaListaCorte.Nivel;

/**
 * Confere as cavidades dos conectores da lista de corte, por referência (C1, C2...).
 *
 * <p>Só confere conector cujo código está no padrão de vias ({@link CodigoConector}, ex.: {@code CNFAT04CZ}
 * = 4 vias). Conector fora do padrão não é conferido e não bloqueia nada. Para os conferidos:
 *
 * <ul>
 * <li><b>ERRO</b>: cavidade numérica que não existe no conector (ex.: cavidade 5 num de 4 vias).</li>
 * <li><b>ERRO</b>: a mesma cavidade usada por mais de um cabo.</li>
 * <li><b>AVISO</b>: a mesma referência com códigos diferentes (confere pelo primeiro código).</li>
 * </ul>
 *
 * Cavidade vazia ou não numérica ("+", "-", "XX") não é conferida. ERRO impede o envio para gerar.
 */
public final class AnaliseConectores {

    private AnaliseConectores() {
    }

    public static List<ProblemaListaCorte> analisar(List<CaboListaCorte> cabos) {
        Map<String, List<Uso>> porRef = new LinkedHashMap<>();
        for (CaboListaCorte cabo : cabos) {
            for (PontaConector p : new PontaConector[] { cabo.a(), cabo.b() }) {
                if (p != null && !p.ref().isEmpty())
                    porRef.computeIfAbsent(p.ref(), k -> new ArrayList<>()).add(new Uso(p, cabo));
            }
        }
        List<ProblemaListaCorte> problemas = new ArrayList<>();
        porRef.forEach((ref, usos) -> analisarConector(ref, usos, problemas));
        return List.copyOf(problemas);
    }

    private static void analisarConector(String ref, List<Uso> usos, List<ProblemaListaCorte> problemas) {
        Set<String> codigos = usos.stream().map(u -> u.ponta().codigo()).filter(c -> !c.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (codigos.size() > 1)
            problemas.add(new ProblemaListaCorte(Nivel.AVISO, ref,
                    "Conector %s aparece com códigos diferentes: %s.".formatted(ref, String.join(", ", codigos))));

        String codigo = codigos.isEmpty() ? "" : codigos.iterator().next();
        OptionalInt vias = CodigoConector.vias(codigo);
        if (vias.isEmpty())
            return; // fora do padrão de vias: não dá para conferir

        Map<String, List<Uso>> porCavidade = new LinkedHashMap<>();
        for (Uso u : usos) {
            String cav = normalizar(u.ponta().cavidade());
            if (cav.isEmpty())
                continue;
            porCavidade.computeIfAbsent(cav, k -> new ArrayList<>()).add(u);
            Integer n = numero(cav);
            if (n != null && (n < 1 || n > vias.getAsInt()))
                problemas.add(new ProblemaListaCorte(Nivel.ERRO, ref,
                        "Conector %s (%s, %d vias): cavidade %s não existe — cabo %s.".formatted(
                                ref, codigo, vias.getAsInt(), cav, u.descricao())));
        }
        porCavidade.forEach((cav, lista) -> {
            if (lista.size() > 1)
                problemas.add(new ProblemaListaCorte(Nivel.ERRO, ref,
                        "Conector %s (%s): cavidade %s duplicada, usada por %d cabos — %s.".formatted(ref, codigo, cav,
                                lista.size(), lista.stream().map(Uso::descricao).collect(Collectors.joining(", ")))));
        });
    }

    /** "03" e "3" são a mesma cavidade. */
    private static String normalizar(String cav) {
        Integer n = numero(cav);
        return n == null ? cav : n.toString();
    }

    private static Integer numero(String cav) {
        if (!cav.matches("\\d{1,3}"))
            return null;
        return Integer.valueOf(cav);
    }

    private record Uso(PontaConector ponta, CaboListaCorte cabo) {
        String descricao() {
            return "%s (%s linha %d)".formatted(cabo.circ(), cabo.aba(), cabo.linha());
        }
    }
}
