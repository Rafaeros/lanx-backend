package br.lanx.api.auth.user;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.departamentos.DepartamentoUsageCheck;

/** Usuários vetam a exclusão de um departamento ao qual ainda pertencem. */
@Component
class UserDepartamentoUsageCheck implements DepartamentoUsageCheck {

    private final UserRepository users;

    UserDepartamentoUsageCheck(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean isInUse(UUID departamentoId) {
        return users.existsByDepartamentoId(departamentoId);
    }
}
