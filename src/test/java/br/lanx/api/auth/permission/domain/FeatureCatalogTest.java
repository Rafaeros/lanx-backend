package br.lanx.api.auth.permission.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.permission.domain.exception.UnknownFeatureException;
import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.shared.authorization.FeatureKey;

class FeatureCatalogTest {

    private static final FeatureKey COMPRAS = FeatureKey.of("compras");
    private static final FeatureKey ANALISE = FeatureKey.of("compras.analise");

    private final FeatureCatalog catalog = FeatureCatalog.of(List.of(
            FeatureDefinition.of("compras", "Compras"),
            FeatureDefinition.of("compras.analise", "Análise", "aprovar"),
            FeatureDefinition.of("compras.cadastros", "Cadastros"),
            FeatureDefinition.of("estoque", "Estoque")));

    @Test
    @DisplayName("roots e children seguem a hierarquia da chave, na ordem declarada")
    void arvore() {
        assertThat(catalog.roots()).extracting(d -> d.key().value()).containsExactly("compras", "estoque");
        assertThat(catalog.children(COMPRAS)).extracting(d -> d.key().value())
                .containsExactly("compras.analise", "compras.cadastros");
    }

    @Test
    @DisplayName("chave duplicada falha o startup")
    void duplicada() {
        assertThatThrownBy(() -> FeatureCatalog.of(List.of(
                FeatureDefinition.of("compras", "A"), FeatureDefinition.of("compras", "B"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("filha sem o pai declarado falha o startup")
    void semPai() {
        assertThatThrownBy(() -> FeatureCatalog.of(List.of(FeatureDefinition.of("compras.analise", "A"))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ensureKnown aceita permissões do catálogo")
    void conhecida() {
        catalog.ensureKnown(new PermissionSet(Map.of(ANALISE, 1), Map.of(ANALISE, Set.of("aprovar"))));
    }

    @Test
    @DisplayName("ensureKnown rejeita funcionalidade e ação desconhecidas")
    void desconhecida() {
        assertThatThrownBy(() -> catalog.ensureKnown(PermissionSet.ofMasks(Map.of(FeatureKey.of("rh"), 1))))
                .isInstanceOf(UnknownFeatureException.class);
        assertThatThrownBy(() -> catalog.ensureKnown(
                new PermissionSet(Map.of(), Map.of(ANALISE, Set.of("exportar")))))
                .isInstanceOf(UnknownFeatureException.class);
    }
}
