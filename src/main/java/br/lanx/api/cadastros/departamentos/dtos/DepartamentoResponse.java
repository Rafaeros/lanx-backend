package br.lanx.api.cadastros.departamentos.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.departamentos.Departamento;

public record DepartamentoResponse(
        UUID id,
        String nome) {
    public static DepartamentoResponse fromEntity(Departamento departamento) {
        return new DepartamentoResponse(departamento.getId(), departamento.getNome());  
    }
}
