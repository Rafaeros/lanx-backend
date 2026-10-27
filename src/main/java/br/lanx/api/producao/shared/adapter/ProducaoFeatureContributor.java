package br.lanx.api.producao.shared.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.producao.shared.ProducaoPermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * O módulo "Producao" e suas funcionalidades (chaves em
 * {@link ProducaoPermissions}).
 * O comunizado não tem CRUD (só consultar e gerar a planilha);
 * as ordens de produção, sim.
 */
@Component
public class ProducaoFeatureContributor implements FeatureCatalogContributor {

        @Override
        public List<FeatureDefinition> features() {
                return List.of(
                                FeatureDefinition.of(ProducaoPermissions.PRODUCAO, "Produção"),
                                FeatureDefinition.report(ProducaoPermissions.COMUNIZADO, "Comunizado",
                                                ProducaoPermissions.IMPORTAR),
                                FeatureDefinition.of(ProducaoPermissions.ORDENS_PRODUCAO, "Ordens de produção"));
        }
}
