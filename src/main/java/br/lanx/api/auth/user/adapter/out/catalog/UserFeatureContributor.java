package br.lanx.api.auth.user.adapter.out.catalog;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

@Component
@Order(10)
class UserFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(FeatureDefinition.of("administracao.usuarios", "Usuários"));
    }
}
