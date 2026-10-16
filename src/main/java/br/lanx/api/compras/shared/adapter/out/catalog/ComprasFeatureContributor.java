package br.lanx.api.compras.shared.adapter.out.catalog;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.compras.shared.ComprasPermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/** O módulo "Compras" e suas funcionalidades (chaves em {@link ComprasPermissions}). */
@Component
public class ComprasFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(
                FeatureDefinition.of(ComprasPermissions.COMPRAS, "Compras"),
                FeatureDefinition.of(ComprasPermissions.ANALISE_PEDIDOS, "Análise de Pedidos",
                        ComprasPermissions.IMPORTAR));
    }
}
