package br.lanx.api.auth.permission.dto;

import java.util.List;

/**
 * Um nó da árvore de funcionalidades, com as permissões que o painel pode
 * marcar.
 */
public record FeatureNode(String key, String label, List<PermissionItem> permissions, List<FeatureNode> children) {

    public record PermissionItem(String code, String action) {
    }
}
