package br.lanx.api.auth.authorization.adapter.in.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.shared.authorization.FeatureKey;
import br.lanx.api.shared.authorization.RequiresAction;
import br.lanx.api.shared.authorization.RequiresFeature;

/**
 * Falha o startup se algum controller aponta para funcionalidade ou ação que
 * não existe no catálogo. Erro de digitação vira erro de deploy, não um 403
 * misterioso em produção.
 */
@Component
class RequiredFeaturesValidator implements SmartInitializingSingleton {

    private final RequestMappingHandlerMapping handlerMapping;
    private final FeatureCatalog catalog;

    RequiredFeaturesValidator(RequestMappingHandlerMapping requestMappingHandlerMapping, FeatureCatalog catalog) {
        this.handlerMapping = requestMappingHandlerMapping;
        this.catalog = catalog;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> problems = new ArrayList<>();
        for (HandlerMethod method : handlerMapping.getHandlerMethods().values()) {
            check(method, problems);
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Permissões inválidas nos controllers:\n - "
                    + String.join("\n - ", problems));
        }
    }

    private void check(HandlerMethod method, List<String> problems) {
        String where = method.getBeanType().getSimpleName() + "#" + method.getMethod().getName();
        RequiresFeature feature = PermissionInterceptor.find(method, RequiresFeature.class);
        RequiresAction action = method.getMethodAnnotation(RequiresAction.class);

        if (feature == null) {
            if (action != null) {
                problems.add(where + ": @RequiresAction sem @RequiresFeature");
            }
            return;
        }
        FeatureKey key;
        try {
            key = FeatureKey.of(feature.value());
        } catch (RuntimeException e) {
            problems.add(where + ": " + e.getMessage());
            return;
        }
        var definition = catalog.find(key);
        if (definition.isEmpty()) {
            problems.add(where + ": funcionalidade desconhecida '" + key + "'");
        } else if (action != null && !definition.get().extraActions().contains(action.value())) {
            problems.add(where + ": ação '" + action.value() + "' não declarada em '" + key + "'");
        }
    }
}
