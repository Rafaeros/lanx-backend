package br.lanx.api.auth.authorization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.authorization.application.port.in.NavigationNode;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.application.service.RoleQueryService;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.auth.authorization.domain.exception.PermissionDeniedException;
import br.lanx.api.auth.fakes.InMemoryPermissionCache;
import br.lanx.api.auth.fakes.InMemoryRoleRepository;
import br.lanx.api.auth.fakes.InMemoryUserRepository;
import br.lanx.api.auth.user.application.service.UserQueryService;
import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.shared.authorization.FeatureKey;

import java.util.List;

class AuthorizationServiceTest {

    private static final FeatureKey COMPRAS = FeatureKey.of("compras");
    private static final FeatureKey ANALISE = FeatureKey.of("compras.analise");
    private static final FeatureKey ESTOQUE = FeatureKey.of("estoque");

    private InMemoryUserRepository users;
    private InMemoryRoleRepository roles;
    private InMemoryPermissionCache cache;
    private AuthorizationService authorization;
    private NavigationService navigation;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        roles = new InMemoryRoleRepository();
        cache = new InMemoryPermissionCache();
        var effective = new EffectivePermissions(new UserQueryService(users, new RoleQueryService(roles)), new RoleQueryService(roles), cache);
        var catalog = FeatureCatalog.of(List.of(
                FeatureDefinition.of("compras", "Compras"),
                FeatureDefinition.of("compras.analise", "Análise", "aprovar"),
                FeatureDefinition.of("estoque", "Estoque")));
        authorization = new AuthorizationService(effective);
        navigation = new NavigationService(effective, catalog);
    }

    private User comprador() {
        Role role = Role.create("COMPRADOR", new PermissionSet(
                Map.of(ANALISE, Action.maskOf(Action.READ, Action.UPDATE)),
                Map.of(ANALISE, Set.of("aprovar"))));
        roles.save(role);
        return users.save(User.create("joao", "hash", Set.of(role.id())));
    }

    @Test
    @DisplayName("permite o que a role dá e nega o resto")
    void autorizaPorRole() {
        User joao = comprador();

        assertThatCode(() -> authorization.authorize(joao.id(), ANALISE, Action.READ)).doesNotThrowAnyException();
        assertThatThrownBy(() -> authorization.authorize(joao.id(), ANALISE, Action.DELETE))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> authorization.authorize(joao.id(), ESTOQUE, Action.READ))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("ação extra só passa se a role tiver")
    void acaoExtra() {
        User joao = comprador();

        assertThatCode(() -> authorization.authorizeAction(joao.id(), ANALISE, "aprovar")).doesNotThrowAnyException();
        assertThatThrownBy(() -> authorization.authorizeAction(joao.id(), ANALISE, "exportar"))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("usuário inexistente ou inativo não tem nenhuma permissão")
    void inexistenteEInativo() {
        assertThatThrownBy(() -> authorization.authorize(UUID.randomUUID(), ANALISE, Action.READ))
                .isInstanceOf(PermissionDeniedException.class);

        User ativo = comprador();
        User inativo = new User(ativo.id(), "joao", "hash", false, ativo.roleIds());
        users.save(inativo);
        assertThatThrownBy(() -> authorization.authorize(ativo.id(), ANALISE, Action.READ))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("resolve uma vez e depois usa o cache até invalidar")
    void usaCache() {
        User joao = comprador();
        authorization.authorize(joao.id(), ANALISE, Action.READ);

        users.save(new User(joao.id(), "joao", "hash", false, joao.roleIds())); // desativa no banco

        assertThatCode(() -> authorization.authorize(joao.id(), ANALISE, Action.READ))
                .as("ainda vale o cache").doesNotThrowAnyException();

        cache.invalidate(joao.id());
        assertThatThrownBy(() -> authorization.authorize(joao.id(), ANALISE, Action.READ))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    @DisplayName("navegação mostra só o que o usuário alcança; pai aparece como contêiner")
    void navegacao() {
        User joao = comprador();

        List<NavigationNode> tree = navigation.navigationFor(joao.id());

        assertThat(tree).extracting(n -> n.key()).containsExactly("compras");
        NavigationNode compras = tree.get(0);
        assertThat(compras.permissions()).isEmpty();
        assertThat(compras.children()).hasSize(1);
        NavigationNode analise = compras.children().get(0);
        assertThat(analise.key()).isEqualTo("compras.analise");
        assertThat(analise.permissions()).containsExactly("READ", "UPDATE");
        assertThat(analise.actions()).containsExactly("aprovar");
    }
}
