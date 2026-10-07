package br.lanx.api.auth.authorization.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.lanx.api.auth.authorization.application.port.in.GetNavigationUseCase;
import br.lanx.api.auth.authorization.application.port.in.NavigationNode;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.shared.authorization.FeatureDefinition;

@Service
public class NavigationService implements GetNavigationUseCase {

    private final EffectivePermissions effective;
    private final FeatureCatalog catalog;

    NavigationService(EffectivePermissions effective, FeatureCatalog catalog) {
        this.effective = effective;
        this.catalog = catalog;
    }

    @Override
    public List<NavigationNode> navigationFor(UUID userId) {
        PermissionSet permissions = effective.of(userId);
        return build(catalog.roots(), permissions);
    }

    private List<NavigationNode> build(List<FeatureDefinition> definitions, PermissionSet permissions) {
        return definitions.stream()
                .map(def -> node(def, permissions))
                .filter(node -> !node.permissions().isEmpty()
                        || !node.actions().isEmpty()
                        || !node.children().isEmpty())
                .toList();
    }

    private NavigationNode node(FeatureDefinition def, PermissionSet permissions) {
        List<String> crud = Arrays.stream(Action.values())
                .filter(a -> permissions.can(def.key(), a))
                .map(Action::name)
                .toList();
        List<String> extras = def.extraActions().stream()
                .filter(a -> permissions.canDo(def.key(), a))
                .sorted()
                .toList();
        List<NavigationNode> children = build(catalog.children(def.key()), permissions);
        return new NavigationNode(def.key().value(), def.label(), crud, extras, children);
    }
}
