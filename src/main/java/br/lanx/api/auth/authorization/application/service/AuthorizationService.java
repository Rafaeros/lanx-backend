package br.lanx.api.auth.authorization.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.lanx.api.auth.authorization.application.port.in.AuthorizeUseCase;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.authorization.domain.exception.PermissionDeniedException;
import br.lanx.api.shared.authorization.FeatureKey;

@Service
public class AuthorizationService implements AuthorizeUseCase {

    private final EffectivePermissions effective;

    AuthorizationService(EffectivePermissions effective) {
        this.effective = effective;
    }

    @Override
    public void authorize(UUID userId, FeatureKey feature, Action action) {
        if (!effective.of(userId).can(feature, action)) {
            throw new PermissionDeniedException();
        }
    }

    @Override
    public void authorizeAction(UUID userId, FeatureKey feature, String action) {
        if (!effective.of(userId).canDo(feature, action)) {
            throw new PermissionDeniedException();
        }
    }
}
