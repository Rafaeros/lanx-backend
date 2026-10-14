package br.lanx.api.auth.user;

import static br.lanx.api.auth.permission.AuthPermissions.USUARIOS_CREATE;
import static br.lanx.api.auth.permission.AuthPermissions.USUARIOS_READ;
import static br.lanx.api.auth.permission.AuthPermissions.USUARIOS_UPDATE;

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

import br.lanx.api.auth.user.dto.RegisterUserRequest;
import br.lanx.api.auth.user.dto.UpdateUserRequest;
import br.lanx.api.auth.user.dto.UserResponse;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth/users")
public class UserController {

    record CreatedResponse(UUID id) {
    }

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + USUARIOS_READ + "')")
    ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Usuário carregado", service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + USUARIOS_CREATE + "')")
    ResponseEntity<ApiResponse<CreatedResponse>> create(@Valid @RequestBody RegisterUserRequest request) {
        return ApiResponse.created("Usuário criado", new CreatedResponse(service.register(request))).toEntity();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + USUARIOS_UPDATE + "')")
    ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        service.update(id, request);
        return ApiResponse.ok("Usuário atualizado", null);
    }
}
