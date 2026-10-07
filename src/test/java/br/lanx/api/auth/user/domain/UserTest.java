package br.lanx.api.auth.user.domain;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.domain.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.user.domain.exception.InvalidUserException;
import br.lanx.api.auth.user.domain.exception.UserInactiveException;
import br.lanx.api.shared.authorization.FeatureKey;

class UserTest {

    private static final FeatureKey COMPRAS = FeatureKey.of("compras");
    private static final FeatureKey FORNECEDORES = FeatureKey.of("fornecedores");

    private static final Role COMPRADOR = Role.create("COMPRADOR",
            PermissionSet.ofMasks(Map.of(COMPRAS, Action.maskOf(Action.READ, Action.UPDATE))));
    private static final Role FORNECEDOR = Role.create("FORNECEDOR",
            PermissionSet.ofMasks(Map.of(FORNECEDORES, Action.maskOf(Action.CREATE))));

    private static User withOverrides(Set<UUID> roleIds, PermissionSet grants, PermissionSet denies) {
        return new User(UUID.randomUUID(), "joao", "hash", true, roleIds, grants, denies);
    }

    @Test
    @DisplayName("create normaliza username e nasce ativo, guardando só o id da role")
    void createValido() {
        User user = User.create("  Joao.Silva ", "hash", Set.of(COMPRADOR.id()));

        assertThat(user.username()).isEqualTo("joao.silva");
        assertThat(user.active()).isTrue();
        assertThat(user.roleIds()).containsExactly(COMPRADOR.id());
    }

    @Test
    @DisplayName("username inválido → InvalidUser")
    void usernameInvalido() {
        assertThatThrownBy(() -> User.create("a", "hash", Set.of(COMPRADOR.id())))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    @DisplayName("sem role → InvalidUser, na criação e na edição")
    void semRole() {
        assertThatThrownBy(() -> User.create("joao", "hash", Set.of()))
                .isInstanceOf(InvalidUserException.class);

        User user = User.create("joao", "hash", Set.of(COMPRADOR.id()));
        assertThatThrownBy(() -> user.update(Set.of(), PermissionSet.EMPTY, PermissionSet.EMPTY, true))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    @DisplayName("permissão efetiva junta as permissões de todas as roles do usuário")
    void efetivaJuntaRoles() {
        User user = User.create("joao", "hash", Set.of(COMPRADOR.id(), FORNECEDOR.id()));

        assertThat(user.effectivePermissions(List.of(COMPRADOR, FORNECEDOR)).authorities())
                .containsExactlyInAnyOrder("compras:READ", "compras:UPDATE", "fornecedores:CREATE");
    }

    @Test
    @DisplayName("roles que não pertencem ao usuário são ignoradas")
    void ignoraRoleAlheia() {
        User user = User.create("joao", "hash", Set.of(COMPRADOR.id()));

        assertThat(user.effectivePermissions(List.of(COMPRADOR, FORNECEDOR)).authorities())
                .containsExactlyInAnyOrder("compras:READ", "compras:UPDATE");
    }

    @Test
    @DisplayName("grant individual soma ao que a role dá")
    void grantIndividual() {
        User user = withOverrides(Set.of(COMPRADOR.id()),
                PermissionSet.ofMasks(Map.of(FORNECEDORES, Action.maskOf(Action.READ))), PermissionSet.EMPTY);

        assertThat(user.effectivePermissions(List.of(COMPRADOR)).authorities())
                .contains("fornecedores:READ", "compras:READ");
    }

    @Test
    @DisplayName("deny individual vence a role")
    void denyVenceRole() {
        User user = withOverrides(Set.of(COMPRADOR.id()), PermissionSet.EMPTY,
                PermissionSet.ofMasks(Map.of(COMPRAS, Action.maskOf(Action.UPDATE))));

        assertThat(user.effectivePermissions(List.of(COMPRADOR)).authorities()).containsExactly("compras:READ");
    }

    @Test
    @DisplayName("deny vence até o grant do próprio usuário")
    void denyVenceGrant() {
        PermissionSet p = PermissionSet.ofMasks(Map.of(FORNECEDORES, Action.maskOf(Action.READ)));
        User user = withOverrides(Set.of(COMPRADOR.id()), p, p);

        assertThat(user.effectivePermissions(List.of(COMPRADOR)).authorities())
                .doesNotContain("fornecedores:READ");
    }

    @Test
    @DisplayName("update troca roles, exceções e situação, mantendo id, username e hash")
    void update() {
        User user = User.create("joao", "hash", Set.of(COMPRADOR.id()));

        User updated = user.update(Set.of(FORNECEDOR.id()), PermissionSet.EMPTY, PermissionSet.EMPTY, false);

        assertThat(updated.id()).isEqualTo(user.id());
        assertThat(updated.username()).isEqualTo("joao");
        assertThat(updated.passwordHash()).isEqualTo("hash");
        assertThat(updated.roleIds()).containsExactly(FORNECEDOR.id());
        assertThat(updated.active()).isFalse();
    }

    @Test
    @DisplayName("inativo não pode logar")
    void inativo() {
        User user = new User(UUID.randomUUID(), "maria", "hash", false, Set.of());

        assertThatThrownBy(user::ensureCanLogin).isInstanceOf(UserInactiveException.class);
    }
}
