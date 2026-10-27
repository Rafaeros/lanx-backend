package br.lanx.api.comercial.shared.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.comercial.shared.ComercialPermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * O módulo "Comercial" e suas funcionalidades (chaves em
 * {@link ComercialPermissions}).
 * A carteira não tem CRUD: só consultar e exportar.
 */
@Component
public class ComercialFeatureContributor implements FeatureCatalogContributor {

        @Override
        public List<FeatureDefinition> features() {
                return List.of(
                                FeatureDefinition.of(ComercialPermissions.COMERCIAL, "Comercial"),
                                FeatureDefinition.report(ComercialPermissions.CARTEIRA, "Carteira de pedidos",
                                                ComercialPermissions.EXPORTAR));
        }
}
