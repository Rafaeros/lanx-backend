package br.lanx.api.auth.role.dto;

import java.util.List;
import java.util.UUID;

import br.lanx.api.auth.role.Role;

public record RoleResponse(UUID id, String name, String description, List<String> permissions) {

    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.permissionCodes().stream().sorted().toList());
    }
}
