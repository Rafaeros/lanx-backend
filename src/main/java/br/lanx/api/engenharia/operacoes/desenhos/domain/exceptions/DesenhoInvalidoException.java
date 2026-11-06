package br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

/** Pedido de desenho com dado inválido (projeto, arquivos); o adaptador web devolve 400. */
public class DesenhoInvalidoException extends DomainException {

    public DesenhoInvalidoException(String mensagem) {
        super(ErrorType.VALIDATION, mensagem);
    }
}
