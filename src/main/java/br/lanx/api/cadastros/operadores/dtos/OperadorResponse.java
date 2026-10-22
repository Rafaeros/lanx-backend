package br.lanx.api.cadastros.operadores.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.operadores.Operador;

public record OperadorResponse(
        UUID id,
        String nome,
        /** Null quando o operador não tem departamento. */
        DepartamentoSummary departamento,
        /** Null quando o operador não está vinculado a um usuário do LANX. */
        UsuarioSummary usuario) {

    public record DepartamentoSummary(UUID id, String nome) {
    }

    public record UsuarioSummary(UUID id, String username) {
    }

    public static OperadorResponse of(Operador operador, String username) {
        Departamento departamento = operador.getDepartamento();
        UUID userId = operador.getUserId();
        return new OperadorResponse(operador.getId(), operador.getNome(),
                departamento == null ? null : new DepartamentoSummary(departamento.getId(), departamento.getNome()),
                userId == null ? null : new UsuarioSummary(userId, username));
    }
}
