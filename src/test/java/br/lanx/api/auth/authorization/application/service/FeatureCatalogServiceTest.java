package br.lanx.api.auth.authorization.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.authorization.application.port.in.FeatureNode;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.shared.authorization.FeatureDefinition;

class FeatureCatalogServiceTest {

    @Test
    @DisplayName("devolve a árvore completa, com ações extras e sem filtrar por usuário")
    void arvoreCompleta() {
        var service = new FeatureCatalogService(FeatureCatalog.of(List.of(
                FeatureDefinition.of("compras", "Compras"),
                FeatureDefinition.of("compras.analise", "Análise", "exportar", "aprovar"),
                FeatureDefinition.of("estoque", "Estoque"))));

        List<FeatureNode> tree = service.featureTree();

        assertThat(tree).extracting(FeatureNode::key).containsExactly("compras", "estoque");
        FeatureNode analise = tree.get(0).children().get(0);
        assertThat(analise.key()).isEqualTo("compras.analise");
        assertThat(analise.label()).isEqualTo("Análise");
        assertThat(analise.actions()).containsExactly("aprovar", "exportar");
    }
}
