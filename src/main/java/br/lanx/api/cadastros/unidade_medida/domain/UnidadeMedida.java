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

        public static UnidadeMedida create(String nome, String sigla, BigDecimal fatorConversao) {
                String normalizedNome = normalizeNome(nome);
                String normalizedSigla = normalizeSigla(sigla);
                BigDecimal fator = fatorConversao != null ? fatorConversao : BigDecimal.ONE;
                return new UnidadeMedida(Ids.newId(), normalizedNome, normalizedSigla, fator);
        }

        public UnidadeMedida update(String novoNome, String novaSigla, BigDecimal novoFator) {
                // Se novoNome for nulo, mantemos o this.nome que já estava na entidade!
                String nomeFinal = (novoNome != null && !novoNome.isBlank()) ? novoNome.trim().toLowerCase()
                                : this.nome;

                // Se novaSigla for nula, mantemos o this.sigla!
                String siglaFinal = (novaSigla != null && !novaSigla.isBlank()) ? novaSigla.trim() : this.sigla;

                // Se o fator novo for nulo, mantemos o this.fatorConversao!
                BigDecimal fatorFinal = (novoFator != null) ? novoFator : this.fatorConversao;

                // O construtor é chamado, revalida tudo, e devolve a versão final!
                return new UnidadeMedida(this.id, nomeFinal, siglaFinal, fatorFinal);
        }

        private static String normalizeNome(String nome) {
                return nome != null ? nome.trim().toLowerCase() : null;
        }

        private static String normalizeSigla(String sigla) {
                return sigla != null ? sigla.trim() : null;
        }
}
