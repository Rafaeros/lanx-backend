package br.lanx.api.shared.domain;

public abstract class DomainException extends RuntimeException {

    private final ErrorType type;

    protected DomainException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public ErrorType type() { return type; }
}