package br.lanx.api.auth.authorization;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.lanx.api.auth.authorization.dto.NavigationNode;
import br.lanx.api.auth.permission.FeatureCatalog;
import br.lanx.api.auth.permission.Permission;
import br.lanx.api.shared.authorization.FeatureDefinition;

/** Monta a árvore de funcionalidades filtrada pelo que o usuário pode fazer. */
@Service
public class NavigationService {

    private final AuthorityService authorities;
    private final FeatureCatalog catalog;

    NavigationService(AuthorityService authorities, FeatureCatalog catalog) {
        this.authorities = authorities;
        this.catalog = catalog;
    }

    public List<NavigationNode> navigationFor(UUID userId) {
        return build(catalog.roots(), authorities.authoritiesOf(userId));
    }

    private List<NavigationNode> build(List<FeatureDefinition> definitions, Set<String> granted) {
        return definitions.stream()
                .map(def -> node(def, granted))
                .filter(node -> !node.actions().isEmpty() || !node.children().isEmpty())
                .toList();
    }

    private NavigationNode node(FeatureDefinition def, Set<String> granted) {
        String feature = def.key().value();
        List<String> actions = FeatureCatalog.actionsOf(def).stream()
                .filter(action -> granted.contains(Permission.codeOf(feature, action)))
                .toList();
        return new NavigationNode(feature, def.label(), actions, build(catalog.children(def.key()), granted));
    }
}
