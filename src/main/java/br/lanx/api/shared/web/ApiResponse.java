package br.lanx.api.shared.web;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data,
        List<FieldError> errors,
        Instant timestamp) {

    public record FieldError(String field, String message) {
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> created(String message, T data){
        return new ApiResponse<>(HttpStatus.CREATED.value(), message, data, null, Instant.now());
    }

    public static ApiResponse<Void> error(HttpStatus status, String message) {
        return new ApiResponse<>(status.value(), message, null, null, Instant.now());
    }

    public static ApiResponse<Void> validation(List<FieldError> errors) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Dados inválidos", null, errors, Instant.now());
    }
}
