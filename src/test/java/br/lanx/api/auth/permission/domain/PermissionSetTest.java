package br.lanx.api.auth.permission.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.permission.domain.exception.InvalidPermissionException;
import br.lanx.api.shared.authorization.FeatureKey;

class PermissionSetTest {

    private static final FeatureKey ANALISE = FeatureKey.of("compras.analise");

    private static PermissionSet of(int mask, String... extra) {
        return new PermissionSet(Map.of(ANALISE, mask), Map.of(ANALISE, Set.of(extra)));
    }

    @Test
    @DisplayName("can e canDo consultam CRUD e ação extra")
    void consulta() {
        PermissionSet p = of(Action.maskOf(Action.READ), "aprovar");

        assertThat(p.can(ANALISE, Action.READ)).isTrue();
        assertThat(p.can(ANALISE, Action.DELETE)).isFalse();
        assertThat(p.canDo(ANALISE, "aprovar")).isTrue();
        assertThat(p.canDo(ANALISE, "exportar")).isFalse();
    }

    @Test
    @DisplayName("union soma bits e ações")
    void union() {
        PermissionSet p = of(Action.maskOf(Action.READ), "aprovar")
                .union(of(Action.maskOf(Action.UPDATE), "exportar"));

        assertThat(p.authorities()).containsExactlyInAnyOrder(
                "compras.analise:READ", "compras.analise:UPDATE",
                "compras.analise:aprovar", "compras.analise:exportar");
    }

    @Test
    @DisplayName("minus remove só os bits e ações negados")
    void minus() {
        PermissionSet p = of(Action.maskOf(Action.READ, Action.UPDATE), "aprovar", "exportar")
                .minus(of(Action.maskOf(Action.UPDATE), "aprovar"));

        assertThat(p.authorities()).containsExactlyInAnyOrder(
                "compras.analise:READ", "compras.analise:exportar");
    }

    @Test
    @DisplayName("máscara fora de 0..15 → InvalidPermission")
    void mascaraInvalida() {
        assertThatThrownBy(() -> PermissionSet.ofMasks(Map.of(ANALISE, 16)))
                .isInstanceOf(InvalidPermissionException.class);
        assertThatThrownBy(() -> PermissionSet.ofMasks(Map.of(ANALISE, -1)))
                .isInstanceOf(InvalidPermissionException.class);
    }

    @Test
    @DisplayName("nome de ação inválido → InvalidPermission")
    void acaoInvalida() {
        assertThatThrownBy(() -> new PermissionSet(Map.of(), Map.of(ANALISE, Set.of("Aprovar Tudo"))))
                .isInstanceOf(InvalidPermissionException.class);
    }
}
