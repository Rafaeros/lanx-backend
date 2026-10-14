package br.lanx.api.cadastros.materiais.exceptions;

import java.util.UUID;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class MaterialNotFoundException extends DomainException {
    public MaterialNotFoundException(UUID id) {
        super(ErrorType.NOT_FOUND, "Material não encontrado: " + id);
    }
}
