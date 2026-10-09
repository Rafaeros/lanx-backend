package br.lanx.api.cadastros.unidade_medida.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import br.lanx.api.shared.domain.Ids;

public record UnidadeMedida(
                UUID id,
                String nome,
                String sigla,
                BigDecimal fatorConversao) {

        public UnidadeMedida {
                Objects.requireNonNull(id, "id");
                if (nome == null || nome.isBlank())
                        throw new IllegalArgumentException("O nome é obrigatório");
                if (sigla == null || sigla.isBlank())
                        throw new IllegalArgumentException("A sigla é obrigatória");

                Objects.requireNonNull(fatorConversao, "fatorConversao");
                if (fatorConversao.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("O fator de conversão deve ser maior que zero");
                }
        }

        public UnidadeMedida(String nome, String sigla) {
                this(Ids.newId(), nome, sigla, BigDecimal.ONE);
        }

        public static UnidadeMedida create(String name, String sigla, BigDecimal fatorConversao) {
                String normalized = name != null ? name.trim().toLowerCase() : null;
                String normalizedSigla = sigla != null ? sigla.trim() : null;
                BigDecimal fator = fatorConversao != null ? fatorConversao : BigDecimal.ONE;
                return new UnidadeMedida(Ids.newId(), normalized, normalizedSigla, fator);
        }

}
