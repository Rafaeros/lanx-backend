package br.lanx.api.auth.authorization.application.port.in;

import java.util.List;
import java.util.UUID;

/** Porta de entrada: a árvore de funcionalidades já filtrada pelo que o usuário pode. */
public interface GetNavigationUseCase {
    List<NavigationNode> navigationFor(UUID userId);
}
