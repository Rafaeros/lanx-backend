package br.lanx.api.producao.operacoes.ordensdeproducao.importacao;

import br.lanx.api.shared.domain.DomainException;
import br.lanx.api.shared.domain.ErrorType;

/** O arquivo inteiro não serve: não é .xlsx, está vazio ou faltam colunas obrigatórias. */
public class PlanilhaOrdensInvalidasException extends DomainException {
    public PlanilhaOrdensInvalidasException(String message) {
        super(ErrorType.VALIDATION, message);
    }
}
