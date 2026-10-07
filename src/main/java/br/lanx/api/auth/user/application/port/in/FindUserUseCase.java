package br.lanx.api.auth.user.application.port.in;

import java.util.Optional;
import java.util.UUID;

import br.lanx.api.auth.user.domain.User;

/** Contrato do módulo user para os outros módulos (authorization): achar um usuário por id. */
public interface FindUserUseCase {
    Optional<User> findById(UUID id);
}
