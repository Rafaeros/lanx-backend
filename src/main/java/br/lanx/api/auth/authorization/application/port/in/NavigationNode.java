package br.lanx.api.auth.authorization.application.port.in;

import java.util.List;

/**
 * Nó da árvore que o frontend usa para montar menu e esconder botões.
 * {@code permissions} são as ações CRUD (READ, CREATE...) e {@code actions} as extras.
 * Um nó sem permissão própria aparece só como contêiner de filhos acessíveis.
 */
public record NavigationNode(
        String key,
        String label,
        List<String> permissions,
        List<String> actions,
        List<NavigationNode> children) {
}
