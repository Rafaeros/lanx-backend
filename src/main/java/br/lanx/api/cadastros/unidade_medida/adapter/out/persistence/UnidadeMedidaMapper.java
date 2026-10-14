package br.lanx.api.cadastros.unidade_medida.adapter.out.persistence;

import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;

public class UnidadeMedidaMapper {

    private UnidadeMedidaMapper() {
    }

    static UnidadeMedida toDomain(UnidadeMedidaEntity e) {
        return new UnidadeMedida(
                e.getId(),
                e.getNome(),
                e.getSigla());
    }

    static UnidadeMedidaEntity toEntity(UnidadeMedida u) {
        return new UnidadeMedidaEntity(
                u.id(),
                u.nome(),
                u.sigla());
    }

}
