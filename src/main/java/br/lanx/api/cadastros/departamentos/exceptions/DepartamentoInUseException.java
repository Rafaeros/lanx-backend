package br.lanx.api.cadastros.departamentos.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class DepartamentoInUseException extends DomainException {
    public DepartamentoInUseException() {
        super(ErrorType.CONFLICT, "Departamento em uso por operadores ou usuários e não pode ser excluído");
    }
}
