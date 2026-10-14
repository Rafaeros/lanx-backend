package br.lanx.api.auth.permission;

import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * O módulo "Administração" e suas funcionalidades (chaves em
 * {@link AuthPermissions}).
 */
@Component
@Order(0)
class AuthFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(
                FeatureDefinition.of(AuthPermissions.ADMINISTRACAO, "Administração"),
                FeatureDefinition.of(AuthPermissions.USUARIOS, "Usuários"),
                FeatureDefinition.of(AuthPermissions.PERFIS, "Perfis e Permissões"));
    }
}
