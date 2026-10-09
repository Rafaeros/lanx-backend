package br.lanx.api.cadastros.shared.adapter.out.catalog;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

@Component
public class CadastrosFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(
                FeatureDefinition.of("cadastros", "Cadastros"),
                FeatureDefinition.of("cadastros.unidade-medida", "Unidades de Medida"));
    }
}
