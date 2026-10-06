package br.lanx.api.modules.auth.infrastructure.web;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.modules.auth.application.LoginCommand;
import br.lanx.api.modules.auth.application.LoginResult;
import br.lanx.api.modules.auth.application.LoginService;
import br.lanx.api.modules.auth.domain.exceptions.InvalidCredentialException;
import br.lanx.api.modules.auth.domain.exceptions.UserInactiveException;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    record LoginResponse(String accessToken, String tokenType, Instant expiresAt) {
    }

    private final LoginService loginService;

    AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginService.login(new LoginCommand(request.username(), request.password()));
        return ApiResponse.ok("Login realizado com sucesso",
                new LoginResponse(result.accessToken(), "Bearer", result.expiresAt()));
    }

    // Tradução exceção de domínio → HTTP fica no adaptador web, não no domínio
    @ExceptionHandler(InvalidCredentialException.class)
    ProblemDetail invalidCredentials(InvalidCredentialException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserInactiveException.class)
    ProblemDetail inactive(UserInactiveException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

}
