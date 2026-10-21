package br.lanx.api.shared.web;

import br.lanx.api.shared.domain.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Qualquer exceção de negócio de qualquer módulo. */
    @ExceptionHandler(DomainException.class)
    ApiResponse<Void> domain(DomainException ex) {
        HttpStatus status = switch (ex.type()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST; // 400
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED; // 401
            case FORBIDDEN -> HttpStatus.FORBIDDEN; // 403
            case NOT_FOUND -> HttpStatus.NOT_FOUND; // 404
            case CONFLICT -> HttpStatus.CONFLICT; // 409
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_CONTENT; // 422
        };
        return ApiResponse.error(status, ex.getMessage());
    }

    /** @Valid falhou: devolve a lista de campos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ApiResponse<Void> validation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ApiResponse.validation(errors);
    }

    /** JSON malformado no corpo. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ApiResponse<Void> unreadable() {
        return ApiResponse.error(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido");
    }

    /**
     * @PreAuthorize negou: sem isto, cairia no handler genérico abaixo e viraria
     *               500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    ApiResponse<Void> accessDenied() {
        return ApiResponse.error(HttpStatus.FORBIDDEN, "Sem permissão para este recurso");
    }

    /**
     * Qualquer outra coisa: loga o detalhe, mas NÃO devolve stack trace ao cliente.
     */
    @ExceptionHandler(Exception.class)
    ApiResponse<Void> unexpected(Exception ex) {
        log.error("Erro não tratado", ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno");
    }
}