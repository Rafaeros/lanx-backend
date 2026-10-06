package br.lanx.api.modules.auth.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    @DisplayName("máscara 0011 em COMPRAS vira READ e CREATE")
    void converteMascaraEmPermissoes() {
        var role = new Role(UUID.randomUUID(), "COMPRADOR", Map.of(
                Feature.COMPRAS, Action.READ.bit() | Action.CREATE.bit(), // 0001 | 0010 = 0011
                Feature.FORNECEDORES, Action.READ.bit())); // 0001

        assertThat(role.authorities()).containsExactlyInAnyOrder(
                "COMPRAS:READ", "COMPRAS:CREATE", "FORNECEDORES:READ");
    }

    @Test
    @DisplayName("máscara 0 não concede nenhuma permissão")
    void mascaraZero() {
        var role = new Role(UUID.randomUUID(), "VISITANTE", Map.of(Feature.COMPRAS, 0));

        assertThat(role.authorities()).isEmpty();
    }
}