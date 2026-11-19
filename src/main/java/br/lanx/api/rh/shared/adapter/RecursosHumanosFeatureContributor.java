package br.lanx.api.rh.shared.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.rh.shared.RecursosHumanosPermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * O módulo "RecursosHumanos" e suas funcionalidades (chaves em
 * {@link RecursosHumanosPermissions}).
 */
@Component
public class RecursosHumanosFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(
                FeatureDefinition.of(RecursosHumanosPermissions.COLABORADORES, "Colaboradores"));
    }
}
