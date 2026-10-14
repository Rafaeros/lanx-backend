package br.lanx.api.auth.role;

import static br.lanx.api.auth.permission.AuthPermissions.PERFIS_CREATE;
import static br.lanx.api.auth.permission.AuthPermissions.PERFIS_READ;
import static br.lanx.api.auth.permission.AuthPermissions.PERFIS_UPDATE;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.role.dto.RoleRequest;
import br.lanx.api.auth.role.dto.RoleResponse;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth/roles")
public class RoleController {

    record CreatedResponse(UUID id) {
    }

    private final RoleService service;

    RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PERFIS_READ + "')")
    ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.ok("Roles carregadas", service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PERFIS_READ + "')")
    ApiResponse<RoleResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Role carregada", service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PERFIS_CREATE + "')")
    ResponseEntity<ApiResponse<CreatedResponse>> create(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.created("Role criada", new CreatedResponse(service.create(request))).toEntity();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PERFIS_UPDATE + "')")
    ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        service.update(id, request);
        return ApiResponse.ok("Role atualizada", null);
    }
}
