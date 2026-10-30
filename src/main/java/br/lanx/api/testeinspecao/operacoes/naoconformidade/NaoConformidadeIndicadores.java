package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeIndicadoresResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeIndicadoresResponse.Grupo;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeIndicadoresResponse.Mes;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeResponse;

/** Cálculo dos indicadores a partir das NCs já filtradas: sem banco, fácil de testar. */
final class NaoConformidadeIndicadores {

    static final String SEM_CADASTRO = "Excluído do cadastro";
    static final String SEM_CLIENTE = "Sem cliente";

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private NaoConformidadeIndicadores() {
    }

    /** {@code inicio}/{@code fim} nulos: os meses vão da primeira à última NC. */
    static NaoConformidadeIndicadoresResponse calcular(List<NaoConformidadeResponse> ncs, LocalDate inicio,
            LocalDate fim) {
        BigDecimal total = ncs.stream().map(NaoConformidadeResponse::quantidadeNaoConforme)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // cada OP entra uma vez na base da taxa, mesmo com várias NCs
        Map<UUID, BigDecimal> ops = new LinkedHashMap<>();
        for (NaoConformidadeResponse nc : ncs) {
            if (nc.ordemDeProducao() != null) {
                ops.putIfAbsent(nc.ordemDeProducao().id(), nc.ordemDeProducao().quantidade());
            }
        }
        BigDecimal quantidadeOps = ops.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return new NaoConformidadeIndicadoresResponse(
                inicio,
                fim,
                ncs.size(),
                total,
                ops.size(),
                quantidadeOps,
                percentual(total, quantidadeOps),
                agrupar(ncs, total, nc -> nc.departamento() != null ? nc.departamento().nome() : SEM_CADASTRO),
                agrupar(ncs, total, nc -> nc.tipo() != null ? nc.tipo().nome() : SEM_CADASTRO),
                agrupar(ncs, total, NaoConformidadeIndicadores::cliente),
                meses(ncs, inicio, fim));
    }

    private static String cliente(NaoConformidadeResponse nc) {
        var op = nc.ordemDeProducao();
        if (op == null) {
            return SEM_CADASTRO;
        }
        return op.clienteNome() != null ? op.clienteNome() : SEM_CLIENTE;
    }

    private static List<Grupo> agrupar(List<NaoConformidadeResponse> ncs, BigDecimal total,
            Function<NaoConformidadeResponse, String> chave) {
        Map<String, long[]> registros = new LinkedHashMap<>();
        Map<String, BigDecimal> quantidades = new LinkedHashMap<>();
        for (NaoConformidadeResponse nc : ncs) {
            String nome = chave.apply(nc);
            registros.computeIfAbsent(nome, k -> new long[1])[0]++;
            quantidades.merge(nome, nc.quantidadeNaoConforme(), BigDecimal::add);
        }

        List<Map.Entry<String, BigDecimal>> ordenados = new ArrayList<>(quantidades.entrySet());
        ordenados.sort(Map.Entry.<String, BigDecimal>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey(Comparator.naturalOrder())));

        List<Grupo> grupos = new ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        for (var entrada : ordenados) {
            acumulado = acumulado.add(entrada.getValue());
            grupos.add(new Grupo(entrada.getKey(), registros.get(entrada.getKey())[0], entrada.getValue(),
                    percentual(entrada.getValue(), total), percentual(acumulado, total)));
        }
        return grupos;
    }

    private static List<Mes> meses(List<NaoConformidadeResponse> ncs, LocalDate inicio, LocalDate fim) {
        LocalDate primeiro = inicio != null ? inicio
                : ncs.stream().map(NaoConformidadeResponse::dataNaoConformidade).min(Comparator.naturalOrder())
                        .orElse(null);
        LocalDate ultimo = fim != null ? fim
                : ncs.stream().map(NaoConformidadeResponse::dataNaoConformidade).max(Comparator.naturalOrder())
                        .orElse(null);
        if (primeiro == null || ultimo == null || primeiro.isAfter(ultimo)) {
            return List.of();
        }

        Map<YearMonth, long[]> registros = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> quantidades = new LinkedHashMap<>();
        for (YearMonth mes = YearMonth.from(primeiro); !mes.isAfter(YearMonth.from(ultimo)); mes = mes.plusMonths(1)) {
            registros.put(mes, new long[1]);
            quantidades.put(mes, BigDecimal.ZERO);
        }
        for (NaoConformidadeResponse nc : ncs) {
            YearMonth mes = YearMonth.from(nc.dataNaoConformidade());
            if (registros.containsKey(mes)) {
                registros.get(mes)[0]++;
                quantidades.merge(mes, nc.quantidadeNaoConforme(), BigDecimal::add);
            }
        }
        return registros.keySet().stream()
                .map(mes -> new Mes(mes.toString(), registros.get(mes)[0], quantidades.get(mes)))
                .toList();
    }

    private static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return null;
        }
        return parte.multiply(CEM).divide(total, 2, RoundingMode.HALF_UP);
    }
}
