package br.lanx.api.auth.authorization.adapter.in.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.authorization.application.port.in.FeatureNode;
import br.lanx.api.auth.authorization.application.port.in.GetFeatureCatalogUseCase;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.web.ApiResponse;

/** Alimenta o painel de administração: o que existe para ser permitido ou negado. */
@RestController
@RequestMapping("/auth/features")
@RequiresFeature("administracao.permissoes")
public class FeatureCatalogController {

    /** crud = ações que toda funcionalidade tem; features = a árvore, com as ações extras de cada nó. */
    record CatalogResponse(List<String> crud, List<FeatureNode> features) {
    }

    private final GetFeatureCatalogUseCase catalog;

    FeatureCatalogController(GetFeatureCatalogUseCase catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    ApiResponse<CatalogResponse> list() {
        var crud = Arrays.stream(Action.values()).map(Action::name).toList();
        return ApiResponse.ok("Catálogo de funcionalidades", new CatalogResponse(crud, catalog.featureTree()));
    }
}
