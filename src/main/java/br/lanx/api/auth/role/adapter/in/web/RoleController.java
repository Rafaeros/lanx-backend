package br.lanx.api.auth.role.adapter.in.web;

import br.lanx.api.auth.permission.adapter.in.web.PermissionsRequest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.auth.role.application.port.in.GetRolesUseCase;
import br.lanx.api.auth.role.application.port.in.GetRolesUseCase.RoleView;
import br.lanx.api.auth.role.application.port.in.RegisterRoleUseCase;
import br.lanx.api.auth.role.application.port.in.RegisterRoleUseCase.RegisterRoleCommand;
import br.lanx.api.auth.role.application.port.in.UpdateRoleUseCase;
import br.lanx.api.auth.role.application.port.in.UpdateRoleUseCase.UpdateRoleCommand;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

/**
 * GET exige READ, POST exige CREATE e PUT exige UPDATE em
 * "administracao.permissoes".
 */
@RestController
@RequestMapping("/auth/roles")
@RequiresFeature("administracao.permissoes")
public class RoleController {

    record CreatedResponse(UUID id) {
    }

    private final RegisterRoleUseCase register;
    private final UpdateRoleUseCase update;
    private final GetRolesUseCase query;

    RoleController(RegisterRoleUseCase register, UpdateRoleUseCase update, GetRolesUseCase query) {
        this.register = register;
        this.update = update;
        this.query = query;
    }

    @GetMapping
    ApiResponse<List<RoleView>> list() {
        return ApiResponse.ok("Roles carregadas", query.roles());
    }

    @GetMapping("/{id}")
    ApiResponse<RoleView> get(@PathVariable UUID id) {
        return ApiResponse.ok("Role carregada", query.role(id));
    }

    @PostMapping
    ResponseEntity<ApiResponse<CreatedResponse>> create(@Valid @RequestBody RoleRequest request) {
        CreatedResult result = register.register(
                new RegisterRoleCommand(request.name(), PermissionsRequest.toInput(request.permissions())));
        return ApiResponse.created("Role criada", new CreatedResponse(result.id())).toEntity();
    }

    @PutMapping("/{id}")
    ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        update.update(new UpdateRoleCommand(id, request.name(), PermissionsRequest.toInput(request.permissions())));
        return ApiResponse.ok("Role atualizada", null);
    }
}
