package br.lanx.api.cadastros.operadores.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.operadores.Operador;

public record OperadorResponse(
        UUID id,
        String nome,
        /** Null quando o operador não tem departamento. */
        DepartamentoSummary departamento) {

    public record DepartamentoSummary(UUID id, String nome) {
    }

    public static OperadorResponse fromEntity(Operador operador) {
        Departamento departamento = operador.getDepartamento();
        return new OperadorResponse(operador.getId(), operador.getNome(),
                departamento == null ? null : new DepartamentoSummary(departamento.getId(), departamento.getNome()));
    }
}
