package br.lanx.api.qualidade.shared.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.qualidade.shared.QualidadePermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * O módulo "Qualidade" e suas funcionalidades (chaves em
 * {@link QualidadePermissions}).
 * O IQF não tem CRUD: só consultar e exportar.
 */
@Component
public class QualidadeFeatureContributor implements FeatureCatalogContributor {

        @Override
        public List<FeatureDefinition> features() {
                return List.of(
                                FeatureDefinition.of(QualidadePermissions.QUALIDADE, "Qualidade"),
                                FeatureDefinition.report(QualidadePermissions.IQF, "IQF de Fornecedores",
                                                QualidadePermissions.EXPORTAR));
        }
}
