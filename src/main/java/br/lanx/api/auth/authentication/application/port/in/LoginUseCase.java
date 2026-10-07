package br.lanx.api.auth.authentication.application.port.in;

/** Porta de entrada: o que o módulo auth oferece para autenticar. */
public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
