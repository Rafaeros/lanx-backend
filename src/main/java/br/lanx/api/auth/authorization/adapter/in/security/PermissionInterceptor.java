package br.lanx.api.auth.authorization.adapter.in.security;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import br.lanx.api.auth.authorization.application.port.in.AuthorizeUseCase;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.user.domain.exception.InvalidCredentialException;
import br.lanx.api.auth.authorization.domain.exception.PermissionDeniedException;
import br.lanx.api.shared.authorization.FeatureKey;
import br.lanx.api.shared.authorization.RequiresAction;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Valida a permissão ANTES de o controller executar. Lê @RequiresFeature (no
 * método ou na classe) e @RequiresAction (no método). Rotas sem anotação exigem
 * só autenticação, que o Spring Security já garantiu.
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final AuthorizeUseCase authorizer;
    private final CurrentUser currentUser;

    PermissionInterceptor(AuthorizeUseCase authorizer, CurrentUser currentUser) {
        this.authorizer = authorizer;
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        RequiresFeature feature = find(method, RequiresFeature.class);
        if (feature == null) {
            return true;
        }

        var userId = userId();
        var key = FeatureKey.of(feature.value());
        RequiresAction extra = method.getMethodAnnotation(RequiresAction.class);

        if (extra != null) {
            authorizer.authorizeAction(userId, key, extra.value());
        } else {
            authorizer.authorize(userId, key, actionFor(request.getMethod()));
        }
        return true;
    }

    /** O método vence a classe: assim um endpoint pode apontar para outra funcionalidade. */
    static <A extends java.lang.annotation.Annotation> A find(HandlerMethod method, Class<A> type) {
        A onMethod = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), type);
        return onMethod != null ? onMethod : AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), type);
    }

    static Action actionFor(String httpMethod) {
        return switch (httpMethod) {
            case "GET", "HEAD" -> Action.READ;
            case "POST" -> Action.CREATE;
            case "PUT", "PATCH" -> Action.UPDATE;
            case "DELETE" -> Action.DELETE;
            default -> throw new PermissionDeniedException();
        };
    }

    private java.util.UUID userId() {
        try {
            return currentUser.id();
        } catch (AuthenticationCredentialsNotFoundException e) {
            throw new InvalidCredentialException();
        }
    }
}
