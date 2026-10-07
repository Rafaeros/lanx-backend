package br.lanx.api.auth.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.user.domain.exception.InvalidUserException;
import br.lanx.api.auth.fakes.FakePasswordHasher;
import br.lanx.api.auth.role.application.service.EnsureRoleService;
import br.lanx.api.auth.fakes.InMemoryPermissionCache;
import br.lanx.api.auth.fakes.InMemoryRoleRepository;
import br.lanx.api.auth.fakes.InMemoryUserRepository;
import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.shared.authorization.FeatureKey;

class AdminBootstrapServiceTest {

    private InMemoryUserRepository users;
    private InMemoryRoleRepository roles;
    private InMemoryPermissionCache cache;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        roles = new InMemoryRoleRepository();
        cache = new InMemoryPermissionCache();
    }

    private AdminBootstrapService serviceWith(FeatureDefinition... features) {
        var catalog = FeatureCatalog.of(List.of(features));
        return new AdminBootstrapService(users, new EnsureRoleService(roles, catalog, cache), new FakePasswordHasher(), catalog);
    }

    @Test
    @DisplayName("cria a role ADMIN com tudo do catálogo e o usuário administrador")
    void cria() {
        serviceWith(FeatureDefinition.of("compras", "Compras", "aprovar"))
                .ensureAdmin("Admin", "senha1234");

        var admin = roles.findByName("ADMIN").orElseThrow();
        assertThat(admin.allows(FeatureKey.of("compras"), Action.DELETE)).isTrue();
        assertThat(admin.permissions().canDo(FeatureKey.of("compras"), "aprovar")).isTrue();

        var user = users.findByUsername("admin").orElseThrow();
        assertThat(user.roleIds()).containsExactly(admin.id());
        assertThat(user.passwordHash()).isEqualTo("hashed:senha1234");
    }

    @Test
    @DisplayName("é idempotente: não duplica nem troca a senha, mas a role ganha funcionalidades novas")
    void idempotenteEResincroniza() {
        serviceWith(FeatureDefinition.of("compras", "Compras")).ensureAdmin("admin", "senha1234");
        var roleId = roles.findByName("ADMIN").orElseThrow().id();

        serviceWith(FeatureDefinition.of("compras", "Compras"), FeatureDefinition.of("estoque", "Estoque"))
                .ensureAdmin("admin", "outraSenha99");

        assertThat(roles.findAll()).hasSize(1);
        assertThat(roles.findByName("ADMIN").orElseThrow().id()).isEqualTo(roleId);
        assertThat(roles.findByName("ADMIN").orElseThrow().allows(FeatureKey.of("estoque"), Action.READ)).isTrue();
        assertThat(users.findByUsername("admin").orElseThrow().passwordHash()).isEqualTo("hashed:senha1234");
    }

    @Test
    @DisplayName("senha fraca do administrador falha a subida")
    void senhaFraca() {
        assertThatThrownBy(() -> serviceWith(FeatureDefinition.of("compras", "Compras")).ensureAdmin("admin", "123"))
                .isInstanceOf(InvalidUserException.class);
    }
}
