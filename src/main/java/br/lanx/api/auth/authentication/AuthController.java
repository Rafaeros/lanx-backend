package br.lanx.api.auth.authentication;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.authentication.dto.FirstAccessRequest;
import br.lanx.api.auth.authentication.dto.LoginRequest;
import br.lanx.api.auth.authentication.dto.LoginResponse;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

/**
 * /auth/login e /auth/first-access são públicos (SecurityConfig.PUBLIC_ENDPOINTS).
 * Credencial inválida vira 401 no GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = service.login(request);
        return ApiResponse.ok(response.firstAccess() ? "Defina a sua senha para continuar"
                : "Login realizado com sucesso", response);
    }

    /**
     * Primeiro acesso: troca a senha provisória (conferida aqui mesmo, sem token)
     * pela senha forte do usuário e devolve o token.
     */
    @PostMapping("/first-access")
    ApiResponse<LoginResponse> firstAccess(@Valid @RequestBody FirstAccessRequest request) {
        return ApiResponse.ok("Senha definida com sucesso", service.completeFirstAccess(request));
    }
}
