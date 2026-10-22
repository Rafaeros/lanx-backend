package br.lanx.api.cadastros.materiais;

import org.springframework.data.jpa.domain.Specification;
import br.lanx.api.cadastros.materiais.dtos.MaterialFilter;

public final class MaterialSpecifications {

    private MaterialSpecifications() {
    }

    public static Specification<Material> byFilter(MaterialFilter filter) {
        if (filter == null) {
            return (root, query, cb) -> cb.conjunction();
        }

        return Specification
                .where(hasCodigo(filter))
                .and(hasDescricao(filter))
                .and(hasTipo(filter));
    }

    /** Filtro ausente = não restringe nada. O Spring Data JPA 4 não aceita Specification nula em and(). */
    private static Specification<Material> matchAll() {
        return (root, query, cb) -> cb.conjunction();
    }

    private static Specification<Material> hasCodigo(MaterialFilter filter) {
        if (filter.codigo() == null || filter.codigo().isBlank()) {
            return matchAll();
        }
        return (root, query, cb) -> cb.equal(
                cb.lower(root.get("codigo")),
                filter.codigo().trim().toLowerCase());
    }

    private static Specification<Material> hasTipo(MaterialFilter filter) {
        if (filter.tipo() == null) {
            return matchAll();
        }
        return (root, query, cb) -> cb.equal(root.get("tipo"), filter.tipo());
    }

    private static Specification<Material> hasDescricao(MaterialFilter filter) {
        if (filter.descricao() == null || filter.descricao().isBlank()) {
            return matchAll();
        }
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("descricao")),
                "%" + filter.descricao().trim().toLowerCase() + "%");
    }
}
