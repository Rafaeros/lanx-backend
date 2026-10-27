package br.lanx.api.cadastros.shared.adapter.out.catalog;

import java.util.List;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.shared.CadastrosPermissions;
import br.lanx.api.shared.authorization.FeatureCatalogContributor;
import br.lanx.api.shared.authorization.FeatureDefinition;

@Component
public class CadastrosFeatureContributor implements FeatureCatalogContributor {

    @Override
    public List<FeatureDefinition> features() {
        return List.of(
                FeatureDefinition.of(CadastrosPermissions.CADASTROS, "Cadastros"),
                FeatureDefinition.of(CadastrosPermissions.UNIDADE_MEDIDA, "Unidades de Medida"),
                FeatureDefinition.of(CadastrosPermissions.MATERIAIS, "Materiais"),
                FeatureDefinition.of(CadastrosPermissions.FORNECEDOR, "Fornecedores"),
                FeatureDefinition.of(CadastrosPermissions.PAIS, "Países"),
                FeatureDefinition.of(CadastrosPermissions.DEPARTAMENTO, "Departamentos"),
                FeatureDefinition.of(CadastrosPermissions.OPERADORES, "Operadores"),
                FeatureDefinition.of(CadastrosPermissions.RATEIOS, "Rateios"),
                FeatureDefinition.of(CadastrosPermissions.COMPRADORES, "Compradores"),
                FeatureDefinition.of(CadastrosPermissions.CLIENTES, "Clientes"));
    }
}
