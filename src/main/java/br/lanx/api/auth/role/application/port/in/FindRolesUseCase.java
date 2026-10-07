package br.lanx.api.auth.role.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import br.lanx.api.auth.role.domain.Role;

/**
 * Contrato do módulo role para os outros módulos (user, authorization):
 * buscar roles por id. Devolve o agregado Role, que faz parte do domínio público.
 */
public interface FindRolesUseCase {
    /** Devolve só as que existem; quem chama compara os tamanhos se precisar. */
    List<Role> findByIds(Collection<UUID> ids);
}
