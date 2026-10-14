package br.lanx.api.auth.authorization.dto;

import java.util.List;

/**
 * Um nó do menu do usuário: {@code actions} são as ações que ele pode fazer ali
 * ("read", "create", "aprovar"). O frontend monta menu e esconde botões com
 * isto.
 */
public record NavigationNode(String key, String label, List<String> actions, List<NavigationNode> children) {
}
