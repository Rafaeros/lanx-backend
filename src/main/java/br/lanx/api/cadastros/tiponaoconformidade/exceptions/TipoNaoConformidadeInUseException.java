package br.lanx.api.cadastros.tiponaoconformidade.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

public class TipoNaoConformidadeInUseException extends DomainException {
    public TipoNaoConformidadeInUseException() {
        super(ErrorType.CONFLICT, "Tipo de não conformidade em uso por não conformidades e não pode ser excluído");
    }
}
