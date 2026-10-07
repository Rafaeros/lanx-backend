package br.lanx.api.auth.authorization.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.lanx.api.auth.authorization.application.port.in.FeatureNode;
import br.lanx.api.auth.authorization.application.port.in.GetFeatureCatalogUseCase;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.shared.authorization.FeatureDefinition;

@Service
public class FeatureCatalogService implements GetFeatureCatalogUseCase {

    private final FeatureCatalog catalog;

    FeatureCatalogService(FeatureCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<FeatureNode> featureTree() {
        return catalog.roots().stream().map(this::node).toList();
    }

    private FeatureNode node(FeatureDefinition def) {
        return new FeatureNode(
                def.key().value(),
                def.label(),
                def.extraActions().stream().sorted().toList(),
                catalog.children(def.key()).stream().map(this::node).toList());
    }
}
