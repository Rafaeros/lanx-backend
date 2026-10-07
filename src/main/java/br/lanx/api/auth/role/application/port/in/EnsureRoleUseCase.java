package br.lanx.api.auth.role.application.port.in;

import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.domain.Role;

/**
 * Contrato do módulo role para quem precisa de uma role garantida (ex.: o
 * bootstrap do administrador): cria se não existe; se existe, substitui as
 * permissões.
 */
public interface EnsureRoleUseCase {
    Role ensure(String name, PermissionSet permissions);
}
