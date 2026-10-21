package br.lanx.api.cadastros.operadores;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.departamentos.DepartamentoUsageCheck;

/** Operadores vetam a exclusão de um departamento ao qual ainda pertencem. */
@Component
class OperadorDepartamentoUsageCheck implements DepartamentoUsageCheck {

    private final OperadorRepository operadores;

    OperadorDepartamentoUsageCheck(OperadorRepository operadores) {
        this.operadores = operadores;
    }

    @Override
    public boolean isInUse(UUID departamentoId) {
        return operadores.existsByDepartamentoId(departamentoId);
    }
}
