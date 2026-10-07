package br.lanx.api.auth.role.domain;

import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.PermissionSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.role.domain.exception.InvalidRoleException;
import br.lanx.api.shared.authorization.FeatureKey;

class RoleTest {

    private static final FeatureKey COMPRAS = FeatureKey.of("compras");
    private static final FeatureKey FORNECEDORES = FeatureKey.of("compras.cadastros.fornecedores");
    private static final FeatureKey IQF = FeatureKey.of("iqf");

    private static PermissionSet masks(FeatureKey k, int mask) {
        return PermissionSet.ofMasks(Map.of(k, mask));
    }

    @Test
    @DisplayName("máscara 0011 vira READ e CREATE")
    void converteMascaraEmPermissoes() {
        var role = new Role(UUID.randomUUID(), "COMPRADOR", PermissionSet.ofMasks(Map.of(
                COMPRAS, Action.READ.bit() | Action.CREATE.bit(),
                FORNECEDORES, Action.READ.bit())));

        assertThat(role.authorities()).containsExactlyInAnyOrder(
                "compras:READ", "compras:CREATE", "compras.cadastros.fornecedores:READ");
    }

    @Test
    @DisplayName("máscara 0 não concede nenhuma permissão")
    void mascaraZero() {
        var role = new Role(UUID.randomUUID(), "VISITANTE", masks(COMPRAS, 0));

        assertThat(role.authorities()).isEmpty();
    }

    @Test
    @DisplayName("create normaliza o nome e gera id")
    void createNormalizaNome() {
        Role role = Role.create("  comprador ", masks(COMPRAS, Action.maskOf(Action.READ)));

        assertThat(role.name()).isEqualTo("COMPRADOR");
        assertThat(role.id()).isNotNull();
    }

    @Test
    @DisplayName("allows consulta feature + ação")
    void allows() {
        Role role = Role.create("COMPRADOR", masks(COMPRAS, Action.maskOf(Action.READ, Action.UPDATE)));

        assertThat(role.allows(COMPRAS, Action.UPDATE)).isTrue();
        assertThat(role.allows(COMPRAS, Action.DELETE)).isFalse();
        assertThat(role.allows(IQF, Action.READ)).isFalse();
    }

    @Test
    @DisplayName("nome inválido → InvalidRole")
    void nomeInvalido() {
        assertThatThrownBy(() -> Role.create("a b", masks(COMPRAS, 1)))
                .isInstanceOf(InvalidRoleException.class);
    }

    @Test
    @DisplayName("sem permissões → InvalidRole")
    void semPermissoes() {
        assertThatThrownBy(() -> Role.create("COMPRADOR", PermissionSet.EMPTY))
                .isInstanceOf(InvalidRoleException.class);
    }
}
