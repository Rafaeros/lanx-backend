package br.lanx.api.auth.permission;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.permission.dto.FeatureNode;
import br.lanx.api.auth.permission.dto.FeatureNode.PermissionItem;
import br.lanx.api.auth.permission.exception.UnknownPermissionException;
import br.lanx.api.shared.authorization.FeatureDefinition;

@Service
public class PermissionService {

    private final PermissionRepository permissions;
    private final FeatureCatalog catalog;

    PermissionService(PermissionRepository permissions, FeatureCatalog catalog) {
        this.permissions = permissions;
        this.catalog = catalog;
    }

    /**
     * Alinha a tabela ao catálogo do código: cria o que falta, atualiza a
     * descrição e remove o que não existe mais (o vínculo com as roles cai junto,
     * por ON DELETE CASCADE).
     */
    @Transactional
    public void sync() {
        Map<String, Permission> existing = permissions.findAll().stream()
                .collect(Collectors.toMap(p -> p.getCode(), Function.identity()));

        Set<String> wanted = new HashSet<>();
        for (FeatureDefinition def : catalog.all()) {
            for (String action : FeatureCatalog.actionsOf(def)) {
                String code = Permission.codeOf(def.key().value(), action);
                String description = def.label() + " - " + action;
                wanted.add(code);

                Permission current = existing.get(code);
                if (current == null) {
                    permissions.save(new Permission(def.key().value(), action, description));
                } else if (!description.equals(current.getDescription())) {
                    current.describe(description);
                }
            }
        }

        List<Permission> orphans = existing.values().stream()
                .filter(p -> !wanted.contains(p.getCode()))
                .toList();
        permissions.deleteAll(orphans);
    }

    /** Resolve os códigos para entidades, ou falha listando os que não existem. */
    @Transactional(readOnly = true)
    public Set<Permission> requireAll(Collection<String> codes) {
        Set<String> requested = Set.copyOf(codes);
        List<Permission> found = permissions.findByCodeIn(requested);

        if (found.size() != requested.size()) {
            Set<String> missing = new HashSet<>(requested);
            found.forEach(p -> missing.remove(p.getCode()));
            throw new UnknownPermissionException(String.join(", ", missing));
        }
        return Set.copyOf(found);
    }

    @Transactional(readOnly = true)
    public List<Permission> findAll() {
        return permissions.findAll();
    }

    /** Árvore para o painel de administração. */
    public List<FeatureNode> featureTree() {
        return catalog.roots().stream().map(this::node).toList();
    }

    private FeatureNode node(FeatureDefinition def) {
        List<PermissionItem> items = FeatureCatalog.actionsOf(def).stream()
                .map(a -> new PermissionItem(Permission.codeOf(def.key().value(), a), a))
                .toList();
        return new FeatureNode(
                def.key().value(),
                def.label(),
                items,
                catalog.children(def.key()).stream().map(this::node).toList());
    }
}
