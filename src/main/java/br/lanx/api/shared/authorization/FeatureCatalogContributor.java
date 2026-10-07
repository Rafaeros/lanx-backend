package br.lanx.api.shared.authorization;

import java.util.List;

/**
 * Contrato entre módulos: cada módulo (compras, fornecedores...) expõe um bean
 * implementando esta interface. O módulo auth coleta todos e monta o catálogo,
 * sem precisar conhecer os módulos.
 */
public interface FeatureCatalogContributor {
    List<FeatureDefinition> features();
}
