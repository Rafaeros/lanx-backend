package br.lanx.api.auth.authorization.adapter.out.catalog;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * Declara o módulo "Administração", pai das funcionalidades de usuários e de
 * perfis. As filhas são declaradas pelos próprios módulos user e role.
 */
@Component
@Order(0)
class AuthFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(FeatureDefinition.of("administracao", "Administração"));
    }
}
