package br.lanx.api.cadastros.pais;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.seeder.paises")
public record PaisSeederProperties(@DefaultValue("classpath:db/seeders/paises.csv") Resource path) {
}
