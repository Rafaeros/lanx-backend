package br.lanx.api.testeinspecao.operacoes.naoconformidade.adapter.out;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Onde os anexos ficam: {@code tipo} escolhe o adapter (por enquanto só "local"). */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(@DefaultValue("local") String tipo, @DefaultValue("./data/anexos") String baseDir) {
}
