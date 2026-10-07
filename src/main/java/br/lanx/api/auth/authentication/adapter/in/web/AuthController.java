package br.lanx.api.auth.authentication.adapter.in.web;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.authentication.application.port.in.LoginCommand;
import br.lanx.api.auth.authentication.application.port.in.LoginResult;
import br.lanx.api.auth.authentication.application.port.in.LoginUseCase;
import br.lanx.api.auth.user.domain.exception.InvalidCredentialException;
import br.lanx.api.auth.user.domain.exception.UserInactiveException;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    record LoginResponse(String accessToken, String tokenType, Instant expiresAt) {
    }

    private final LoginUseCase loginUseCase;

    AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.username(), request.password()));
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
