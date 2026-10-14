package br.lanx.api.auth.authorization;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.authorization.dto.NavigationNode;
import br.lanx.api.shared.security.CurrentUser;
import br.lanx.api.shared.web.ApiResponse;

/**
 * Rotas "sobre mim": exigem só autenticação (a regra padrão do SecurityConfig).
 */
@RestController
@RequestMapping("/auth/me")
public class MeController {

    private final NavigationService navigation;
    private final CurrentUser currentUser;

    MeController(NavigationService navigation, CurrentUser currentUser) {
        this.navigation = navigation;
        this.currentUser = currentUser;
    }

    @GetMapping("/navigation")
    ApiResponse<List<NavigationNode>> navigation() {
        return ApiResponse.ok("Navegação carregada", navigation.navigationFor(currentUser.id()));
    }
}
