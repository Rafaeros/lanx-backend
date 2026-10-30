package br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * Query params de GET /teste-inspecao/operacoes/nao-conformidade (e /exportar).
 * Período pela data da não conformidade; sem datas, sem limite.
 */
public record NaoConformidadeFiltro(
        String busca,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

    /** Limites usados no lugar de null: parâmetro nulo de data na query confunde o Postgres. */
    private static final LocalDate SEM_INICIO = LocalDate.of(1900, 1, 1);
    private static final LocalDate SEM_FIM = LocalDate.of(9999, 12, 31);

    public LocalDate inicio() {
        return dataInicio != null ? dataInicio : SEM_INICIO;
    }

    public LocalDate fim() {
        return dataFim != null ? dataFim : SEM_FIM;
    }
}
