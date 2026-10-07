package br.lanx.api.shared.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeatureKeyTest {

    @Test
    @DisplayName("module e parent saem do caminho")
    void hierarquia() {
        FeatureKey key = FeatureKey.of("compras.analise.relatorios");

        assertThat(key.module()).isEqualTo("compras");
        assertThat(key.parent()).contains(FeatureKey.of("compras.analise"));
        assertThat(FeatureKey.of("compras").parent()).isEmpty();
    }

    @Test
    @DisplayName("formato inválido → InvalidFeatureKey")
    void invalida() {
        assertThatThrownBy(() -> FeatureKey.of("Compras")).isInstanceOf(InvalidFeatureKeyException.class);
        assertThatThrownBy(() -> FeatureKey.of("compras..x")).isInstanceOf(InvalidFeatureKeyException.class);
        assertThatThrownBy(() -> FeatureKey.of("compras.analise.relatorios.a.b"))
                .isInstanceOf(InvalidFeatureKeyException.class);
        assertThatThrownBy(() -> FeatureKey.of(null)).isInstanceOf(InvalidFeatureKeyException.class);
    }
}
