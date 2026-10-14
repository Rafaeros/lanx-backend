package br.lanx.api.shared.pagination;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/** Resultado paginado exposto pela aplicação, sem expor a implementação do Spring Data na API. */
public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /** Cria o resultado preservando os metadados da página do Spring Data. */
    public static <T> PageResult<T> from(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    /** Mapeia o conteúdo da página e preserva seus metadados. */
    public static <S, T> PageResult<T> from(Page<S> page, Function<S, T> mapper) {
        return from(page.map(mapper));
    }

    /** Converte os itens mantendo os metadados. */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(items.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
