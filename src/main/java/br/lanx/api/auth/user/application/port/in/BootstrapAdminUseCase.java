package br.lanx.api.auth.user.application.port.in;

/**
 * Garante que existe um administrador. Sem ele ninguém consegue cadastrar o
 * primeiro usuário, já que todas as rotas de administração exigem permissão.
 */
public interface BootstrapAdminUseCase {
    void ensureAdmin(String username, String rawPassword);
}
