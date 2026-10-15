package br.lanx.api.cadastros.unidademedida.domain;

import java.util.Objects;
import java.util.UUID;

import br.lanx.api.shared.domain.Ids;

public record UnidadeMedida(
                UUID id,
                String nome,
                String sigla) {


        public UnidadeMedida {
                Objects.requireNonNull(id, "id");
                if (nome == null || nome.isBlank())
                        throw new IllegalArgumentException("O nome é obrigatório");
                if (sigla == null || sigla.isBlank())
                        throw new IllegalArgumentException("A sigla é obrigatória");
        }

        public UnidadeMedida(String nome, String sigla) {
                this(Ids.newId(), nome, sigla);
        }

        public static UnidadeMedida create(String nome, String sigla) {
                String novoNome = (nome != null && !nome.isBlank()) ? nome.trim() : null;
                String novaSigla = (sigla != null && !sigla.isBlank()) ? sigla.trim() : null;
                return new UnidadeMedida(Ids.newId(), novoNome, novaSigla);

        }

        public UnidadeMedida update(String novoNome, String novaSigla) {
                // Se novoNome for nulo, mantemos o this.nome que já estava na entidade!
                String nomeFinal = (novoNome != null && !novoNome.isBlank()) ? novoNome.trim()
                                : this.nome;

                // Se novaSigla for nula, mantemos o this.sigla!
                String siglaFinal = (novaSigla != null && !novaSigla.isBlank()) ? novaSigla.trim() : this.sigla;

                // O construtor é chamado, revalida tudo, e devolve a versão final!
                return new UnidadeMedida(this.id, nomeFinal, siglaFinal);

        }
}