package br.lanx.api.shared.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 401/403 gerados pelo Spring Security ANTES de chegar ao controller.
 * O GlobalExceptionHandler não enxerga esses casos, por isso o JSON é escrito
 * aqui,
 * no mesmo formato do ApiResponse.
 */
class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override // 401: sem token, token inválido ou expirado
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
            throws IOException {
        write(res, HttpStatus.UNAUTHORIZED, "Autenticação necessária");
    }

    @Override // 403: autenticado, mas sem a permissão
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
            throws IOException {
        write(res, HttpStatus.FORBIDDEN, "Sem permissão para este recurso");
    }

    private void write(HttpServletResponse res, HttpStatus status, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("""
                {"status":%d,"message":"%s","timestamp":"%s"}"""
                .formatted(status.value(), message, Instant.now()));
    }
}