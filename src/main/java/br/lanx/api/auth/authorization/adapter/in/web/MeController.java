package br.lanx.api.auth.authorization.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.authorization.application.port.in.GetNavigationUseCase;
import br.lanx.api.auth.authorization.application.port.in.NavigationNode;
import br.lanx.api.shared.security.CurrentUser;
import br.lanx.api.shared.web.ApiResponse;

/** Rotas "sobre mim": só exigem autenticação. */
@RestController
@RequestMapping("/auth/me")
public class MeController {

    private final GetNavigationUseCase navigation;
    private final CurrentUser currentUser;

    MeController(GetNavigationUseCase navigation, CurrentUser currentUser) {
        this.navigation = navigation;
        this.currentUser = currentUser;
    }

    /**
     * Árvore de funcionalidades filtrada: o frontend monta menu e esconde botões
     * com ela.
     */
    @GetMapping("/navigation")
    ApiResponse<List<NavigationNode>> navigation() {
        return ApiResponse.ok("Navegação carregada", navigation.navigationFor(currentUser.id()));
    }
}
