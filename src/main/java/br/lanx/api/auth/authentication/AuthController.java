package br.lanx.api.auth.authentication;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.authentication.dto.LoginRequest;
import br.lanx.api.auth.authentication.dto.LoginResponse;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

/**
 * /auth/login é público (SecurityConfig.PUBLIC_ENDPOINTS). Credencial inválida
 * vira 401 no GlobalExceptionHandler.
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
        return ApiResponse.ok("Login realizado com sucesso", service.login(request));
    }
}
