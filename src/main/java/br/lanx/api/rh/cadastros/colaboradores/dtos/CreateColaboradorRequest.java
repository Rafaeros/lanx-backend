package br.lanx.api.rh.cadastros.colaboradores.dtos;

import java.util.List;
import java.util.UUID;

import org.hibernate.validator.constraints.br.CPF;

import br.lanx.api.cadastros.enderecos.dtos.EnderecoRequest;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoEmail;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoEndereco;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoTelefone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateColaboradorRequest(
    @NotBlank(message = "O nome é obrigatório") String nome,
    @CPF @NotBlank String cpf,
    @NotNull UUID departamentoId,
    List<@Valid Telefone> telefones,
    List<@Valid Email> emails,
    List<@Valid Endereco> enderecos) {

    public CreateColaboradorRequest {
        telefones = telefones == null ? List.of() : List.copyOf(telefones);
        emails = emails == null ? List.of() : List.copyOf(emails);
        enderecos = enderecos == null ? List.of() : List.copyOf(enderecos);
    }

    public record Telefone(
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Telefone deve estar no formato +5511999998888") @NotBlank(message = "O telefone é obrigatório") @Size(max = 16) String telefone,
        TipoTelefone tipo,
        boolean principal) {
        public Telefone {
            tipo = tipo == null ? TipoTelefone.CELULAR : tipo;
        }
    }

    public record Email(
        @NotBlank(message = "O email é obrigatório") @Size(max = 255) @jakarta.validation.constraints.Email String email,
        TipoEmail tipo,
        boolean principal) {
        public Email {
            tipo = tipo == null ? TipoEmail.PESSOAL : tipo;
        }
    }

    public record Endereco(
        @Valid @NotNull EnderecoRequest endereco,
        TipoEndereco tipo,
        boolean principal) {
        public Endereco {
            tipo = tipo == null ? TipoEndereco.RESIDENCIAL : tipo;
        }
    }
}
