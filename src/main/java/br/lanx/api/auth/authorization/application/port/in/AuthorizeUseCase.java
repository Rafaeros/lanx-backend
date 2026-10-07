package br.lanx.api.auth.authorization.application.port.in;

import java.util.UUID;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.shared.authorization.FeatureKey;

/** Porta de entrada: "este usuário pode fazer isto?". Lança PermissionDeniedException se não. */
public interface AuthorizeUseCase {
    void authorize(UUID userId, FeatureKey feature, Action action);

    void authorizeAction(UUID userId, FeatureKey feature, String action);
}
