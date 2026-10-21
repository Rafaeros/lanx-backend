package br.lanx.api.shared.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Aplica o "status" do {@link ApiResponse} como status HTTP da resposta. Assim
 * controllers e handlers só devolvem o ApiResponse (ex.: {@code ApiResponse.created(...)})
 * e o status HTTP e o do corpo nunca divergem.
 */
@RestControllerAdvice
class ApiResponseStatusAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof ApiResponse<?> apiResponse) {
            response.setStatusCode(HttpStatus.valueOf(apiResponse.status()));
        }
        return body;
    }
}
