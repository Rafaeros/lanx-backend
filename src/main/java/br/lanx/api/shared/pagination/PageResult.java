package br.lanx.api.shared.pagination;

import java.util.List;
import java.util.function.Function;

/** Saída paginada. Sem Spring: o caso de uso devolve isto, e o adaptador JPA monta a partir do Page do Spring Data. */
public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /** Converte os itens mantendo os metadados: PageResult<Pedido> → PageResult<PedidoResponse> */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(items.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}