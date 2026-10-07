package br.lanx.api.auth.user.adapter.in.web;

import br.lanx.api.auth.permission.adapter.in.web.PermissionsRequest;

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
import br.lanx.api.auth.user.application.port.in.GetUserUseCase;
import br.lanx.api.auth.user.application.port.in.GetUserUseCase.UserView;
import br.lanx.api.auth.user.application.port.in.RegisterUserUseCase;
import br.lanx.api.auth.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import br.lanx.api.auth.user.application.port.in.UpdateUserUseCase;
import br.lanx.api.auth.user.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

/** GET exige READ, POST exige CREATE e PUT exige UPDATE em "administracao.usuarios". */
@RestController
@RequestMapping("/auth/users")
@RequiresFeature("administracao.usuarios")
public class UserController {

    record CreatedResponse(UUID id) {
    }

    private final RegisterUserUseCase register;
    private final UpdateUserUseCase update;
    private final GetUserUseCase query;

    UserController(RegisterUserUseCase register, UpdateUserUseCase update, GetUserUseCase query) {
        this.register = register;
        this.update = update;
        this.query = query;
    }

    @GetMapping("/{id}")
    ApiResponse<UserView> get(@PathVariable UUID id) {
        return ApiResponse.ok("Usuário carregado", query.user(id));
    }

    @PostMapping
    ResponseEntity<ApiResponse<CreatedResponse>> create(@Valid @RequestBody RegisterUserRequest request) {
        CreatedResult result = register.register(new RegisterUserCommand(
                request.username(),
                request.password(),
                request.roleIds(),
                PermissionsRequest.toInput(request.grants()),
                PermissionsRequest.toInput(request.denies())));
        return ApiResponse.created("Usuário criado", new CreatedResponse(result.id())).toEntity();
    }

    @PutMapping("/{id}")
    ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        update.update(new UpdateUserCommand(
                id,
                request.roleIds(),
                PermissionsRequest.toInput(request.grants()),
                PermissionsRequest.toInput(request.denies()),
                request.active()));
        return ApiResponse.ok("Usuário atualizado", null);
    }
}
