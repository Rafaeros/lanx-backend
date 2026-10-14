package br.lanx.api.auth.authorization;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.lanx.api.auth.permission.PermissionService;
import br.lanx.api.auth.role.Role;
import br.lanx.api.auth.role.RoleService;
import br.lanx.api.auth.user.UserService;

/**
 * Na subida, nesta ordem: (1) tabela de permissões = catálogo do código;
 * (2) role ADMIN com todas elas; (3) usuário administrador, se houver senha
 * configurada. Sem o admin, ninguém cadastra o primeiro usuário, já que toda
 * rota de administração exige permissão.
 */
@Component
class AuthorizationBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationBootstrap.class);

    private final PermissionService permissions;
    private final RoleService roles;
    private final UserService users;
    private final AdminProperties admin;

    AuthorizationBootstrap(PermissionService permissions, RoleService roles, UserService users,
            AdminProperties admin) {
        this.permissions = permissions;
        this.roles = roles;
        this.users = users;
        this.admin = admin;
    }

    @Override
    public void run(ApplicationArguments args) {
        permissions.sync();
        Role adminRole = roles.ensureAdmin(new HashSet<>(permissions.findAll()));

        if (admin.password() == null || admin.password().isBlank()) {
            log.info("ADMIN_PASSWORD vazio: usuário administrador não será criado");
            return;
        }
        users.ensureAdmin(admin.username(), admin.password(), adminRole);
        log.info("Administrador '{}' garantido", admin.username());
    }
}
