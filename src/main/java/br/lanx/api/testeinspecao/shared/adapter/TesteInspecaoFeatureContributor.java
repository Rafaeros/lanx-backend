package br.lanx.api.testeinspecao.shared.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.testeinspecao.shared.TesteInspecaoPermissions;

/**
 * O módulo "Teste e Inspeção" e suas funcionalidades (chaves em
 * {@link TesteInspecaoPermissions}).
 * Não conformidades têm CRUD e exportação em planilha.
 */
@Component
public class TesteInspecaoFeatureContributor implements FeatureCatalogContributor {

        @Override
        public List<FeatureDefinition> features() {
                return List.of(
                                FeatureDefinition.of(TesteInspecaoPermissions.TESTE_INSPECAO, "Teste e Inspeção"),
                                FeatureDefinition.of(TesteInspecaoPermissions.NAOCONFORMIDADES, "Não Conformidades",
                                                TesteInspecaoPermissions.EXPORTAR));
        }
}
