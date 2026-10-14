package br.lanx.api.auth.permission;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.lanx.api.shared.authorization.FeatureCatalogContributor;

/** Junta o que todos os módulos declararam num único catálogo em memória. */
@Configuration
class FeatureCatalogConfig {

    @Bean
    FeatureCatalog featureCatalog(List<FeatureCatalogContributor> contributors) {
        return FeatureCatalog.of(contributors.stream()
                .flatMap(c -> c.features().stream())
                .toList());
    }
}
