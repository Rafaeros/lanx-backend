package br.lanx.api.auth.authorization.application.port.in;

import java.util.List;

/**
 * Nó do catálogo completo (sem filtrar por usuário), usado para montar a grade
 * de checkboxes no painel de administração. {@code actions} são as ações extras
 * que esta funcionalidade aceita, além do CRUD.
 */
public record FeatureNode(
        String key,
        String label,
        List<String> actions,
        List<FeatureNode> children) {
}
