package br.lanx.api.cadastros.materiais;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.unidademedida.application.port.out.UnidadeMedidaUsageCheck;

/** Materiais vetam a exclusão de uma unidade de medida que ainda usam. */
@Component
class MaterialUnidadeMedidaUsageCheck implements UnidadeMedidaUsageCheck {

    private final MaterialRepository materiais;

    MaterialUnidadeMedidaUsageCheck(MaterialRepository materiais) {
        this.materiais = materiais;
    }

    @Override
    public boolean isInUse(UUID unidadeMedidaId) {
        return materiais.existsByUnidadeMedidaId(unidadeMedidaId);
    }
}
