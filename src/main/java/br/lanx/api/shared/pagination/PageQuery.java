package br.lanx.api.shared.pagination;

/** Entrada: qual página o cliente quer. Limita o size para ninguém pedir 1 milhão de linhas. */
public record PageQuery(int page, int size) {

    private static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }
}