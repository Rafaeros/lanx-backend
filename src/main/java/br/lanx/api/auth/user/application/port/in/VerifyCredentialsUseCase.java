package br.lanx.api.auth.user.application.port.in;

import java.util.UUID;

/**
 * Contrato do módulo user para o módulo authentication: confirmar quem é o
 * usuário. As credenciais pertencem ao user (hash, política de senha, situação);
 * quem emite o token é a authentication.
 */
public interface VerifyCredentialsUseCase {
    /**
     * @throws br.lanx.api.auth.user.domain.exception.InvalidCredentialException usuário
     *         inexistente OU senha errada (mesma resposta, para não revelar contas)
     * @throws br.lanx.api.auth.user.domain.exception.UserInactiveException senha
     *         correta, mas usuário inativo
     */
    VerifiedUser verify(String username, String rawPassword);

    record VerifiedUser(UUID id, String username) {
    }
}
