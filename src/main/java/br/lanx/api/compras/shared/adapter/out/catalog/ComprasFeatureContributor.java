package br.lanx.api.compras.shared.adapter.out.catalog;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.compras.shared.ComprasPermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * O módulo "Compras" e suas funcionalidades (chaves em
 * {@link ComprasPermissions}).
 * Os relatórios não têm CRUD: só visualizar, exportar e (na análise) importar.
 */
@Component
public class ComprasFeatureContributor implements FeatureCatalogContributor {

        @Override
        public List<FeatureDefinition> features() {
                return List.of(
                                FeatureDefinition.of(ComprasPermissions.COMPRAS, "Compras"),
                                FeatureDefinition.report(ComprasPermissions.ANALISE_PEDIDOS, "Análise de Pedidos",
                                                ComprasPermissions.IMPORTAR, ComprasPermissions.EXPORTAR));
        }
}
