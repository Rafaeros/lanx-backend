package br.lanx.api.engenharia.shared.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.engenharia.shared.EngenhariaPermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

/**
 * O módulo "Engenharia" e suas funcionalidades (chaves em {@link EngenhariaPermissions}). As permissões
 * aparecem sozinhas na tela de perfis: basta marcar para quem pode ver, gerar e reprocessar desenhos.
 */
@Component
public class EngenhariaFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(
                FeatureDefinition.of(EngenhariaPermissions.ENGENHARIA, "Engenharia"),
                FeatureDefinition.of(EngenhariaPermissions.DESENHOS, "Desenhos de Chicote",
                        EngenhariaPermissions.REPROCESSAR));
    }
}
