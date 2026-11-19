package br.lanx.api.rh.cadastros.colaboradores.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.lanx.api.rh.cadastros.colaboradores.enums.TipoEmail;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoEndereco;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoTelefone;

public record ColaboradorDetailResponse(
    UUID id,
    String nome,
    String cpf,
    UUID departamentoId,
    String departamentoNome,
    List<Telefone> telefones,
    List<Email> emails,
    List<Endereco> enderecos,
    Instant createdAt,
    Instant updatedAt) {

    public ColaboradorDetailResponse {
        telefones = telefones == null ? List.of() : List.copyOf(telefones);
        emails = emails == null ? List.of() : List.copyOf(emails);
        enderecos = enderecos == null ? List.of() : List.copyOf(enderecos);
    }

    public record Telefone(
        UUID id,
        String telefone,
        TipoTelefone tipo,
        boolean principal,
        Instant createdAt) {
    }

    public record Email(
        UUID id,
        String email,
        TipoEmail tipo,
        boolean principal,
        Instant createdAt) {
    }

    public record Endereco(
        UUID enderecoId,
        TipoEndereco tipo,
        boolean principal,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        Integer cidadeId,
        String cidadeNome) {
    }
}
