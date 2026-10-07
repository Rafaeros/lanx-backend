package br.lanx.api.auth.authorization.application.port.in;

import java.util.List;

/** Porta de entrada: todas as funcionalidades do sistema, em árvore. */
public interface GetFeatureCatalogUseCase {
    List<FeatureNode> featureTree();
}
