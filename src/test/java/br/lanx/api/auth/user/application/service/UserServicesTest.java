package br.lanx.api.auth.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.auth.user.application.port.in.GetUserUseCase.UserView;
import br.lanx.api.auth.permission.application.port.in.PermissionsInput;
import br.lanx.api.auth.user.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import br.lanx.api.auth.user.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.application.service.RoleQueryService;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.auth.user.domain.exception.InvalidUserException;
import br.lanx.api.auth.role.domain.exception.RoleNotFoundException;
import br.lanx.api.auth.permission.domain.exception.UnknownFeatureException;
import br.lanx.api.auth.user.domain.exception.UserNotFoundException;
import br.lanx.api.auth.user.domain.exception.UsernameAlreadyExistsException;
import br.lanx.api.auth.fakes.FakePasswordHasher;
import br.lanx.api.auth.fakes.InMemoryPermissionCache;
import br.lanx.api.auth.fakes.InMemoryRoleRepository;
import br.lanx.api.auth.fakes.InMemoryUserRepository;
import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.shared.authorization.FeatureKey;

class UserServicesTest {

    private static final FeatureKey ANALISE = FeatureKey.of("compras.analise");
    private static final FeatureKey ESTOQUE = FeatureKey.of("estoque");

    private InMemoryUserRepository users;
    private InMemoryRoleRepository roles;
    private InMemoryPermissionCache cache;
    private RegisterUserService register;
    private UpdateUserService update;
    private UserQueryService query;
    private RoleQueryService findRoles;
    private Role comprador;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        roles = new InMemoryRoleRepository();
        cache = new InMemoryPermissionCache();
        findRoles = new RoleQueryService(roles);
        var catalog = FeatureCatalog.of(List.of(
                FeatureDefinition.of("compras", "Compras"),
                FeatureDefinition.of("compras.analise", "Análise", "aprovar"),
                FeatureDefinition.of("estoque", "Estoque")));
        register = new RegisterUserService(users, findRoles, new FakePasswordHasher(), catalog);
        update = new UpdateUserService(users, findRoles, catalog, cache);
        query = new UserQueryService(users, findRoles);
        comprador = roles.save(Role.create("COMPRADOR",
                PermissionSet.ofMasks(Map.of(ANALISE, Action.maskOf(Action.READ, Action.UPDATE)))));
    }

    private RegisterUserCommand command(String username, String password, Set<UUID> roleIds) {
        return new RegisterUserCommand(username, password, roleIds, null, null);
    }

    @Test
    @DisplayName("cria o usuário com username normalizado, senha com hash e a role vinculada")
    void cria() {
        UUID id = register.register(command(" Joao ", "senha1234", Set.of(comprador.id()))).id();

        User saved = users.findById(id).orElseThrow();
        assertThat(saved.username()).isEqualTo("joao");
        assertThat(saved.passwordHash()).isEqualTo("hashed:senha1234");
        assertThat(saved.active()).isTrue();
        assertThat(saved.roleIds()).containsExactly(comprador.id());
    }

    @Test
    @DisplayName("username repetido → conflito; senha fraca → inválido; sem role → inválido")
    void rejeita() {
        register.register(command("joao", "senha1234", Set.of(comprador.id())));

        assertThatThrownBy(() -> register.register(command("JOAO", "senha1234", Set.of(comprador.id()))))
                .isInstanceOf(UsernameAlreadyExistsException.class);
        assertThatThrownBy(() -> register.register(command("maria", "fraca", Set.of(comprador.id()))))
                .isInstanceOf(InvalidUserException.class);
        assertThatThrownBy(() -> register.register(command("maria", "senha1234", Set.of())))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    @DisplayName("role inexistente → não encontrada")
    void roleInexistente() {
        assertThatThrownBy(() -> register.register(command("maria", "senha1234", Set.of(UUID.randomUUID()))))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    @DisplayName("exceções (grants e denies) são gravadas e validadas contra o catálogo")
    void excecoes() {
        var grants = new PermissionsInput(Map.of("estoque", Set.of("READ")), null);
        var denies = new PermissionsInput(Map.of("compras.analise", Set.of("UPDATE")), null);

        UUID id = register.register(new RegisterUserCommand(
                "joao", "senha1234", Set.of(comprador.id()), grants, denies)).id();

        assertThat(users.findById(id).orElseThrow().effectivePermissions(roles.findAll()).authorities())
                .containsExactlyInAnyOrder("compras.analise:READ", "estoque:READ");

        var desconhecida = new PermissionsInput(Map.of("rh", Set.of("READ")), null);
        assertThatThrownBy(() -> register.register(new RegisterUserCommand(
                "maria", "senha1234", Set.of(comprador.id()), desconhecida, null)))
                .isInstanceOf(UnknownFeatureException.class);
    }

    @Test
    @DisplayName("edita roles, exceções e situação, e invalida o cache só deste usuário")
    void edita() {
        UUID id = register.register(command("joao", "senha1234", Set.of(comprador.id()))).id();
        UUID outro = UUID.randomUUID();
        cache.put(id, PermissionSet.EMPTY);
        cache.put(outro, PermissionSet.EMPTY);

        update.update(new UpdateUserCommand(id, Set.of(comprador.id()),
                new PermissionsInput(Map.of("estoque", Set.of("READ", "CREATE")), null), null, false));

        User edited = users.findById(id).orElseThrow();
        assertThat(edited.active()).isFalse();
        assertThat(edited.username()).isEqualTo("joao");
        assertThat(edited.passwordHash()).isEqualTo("hashed:senha1234");
        assertThat(edited.grants().can(ESTOQUE, Action.CREATE)).isTrue();
        assertThat(cache.get(id)).isEmpty();
        assertThat(cache.get(outro)).isPresent();
    }

    @Test
    @DisplayName("editar usuário inexistente → não encontrado")
    void editaInexistente() {
        assertThatThrownBy(() -> update.update(new UpdateUserCommand(
                UUID.randomUUID(), Set.of(comprador.id()), null, null, true)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("consulta mostra roles, exceções e o resultado efetivo")
    void consulta() {
        UUID id = register.register(new RegisterUserCommand("joao", "senha1234", Set.of(comprador.id()),
                new PermissionsInput(Map.of("estoque", Set.of("READ")), null), null)).id();

        UserView view = query.user(id);

        assertThat(view.username()).isEqualTo("joao");
        assertThat(view.roles()).extracting(r -> r.name()).containsExactly("COMPRADOR");
        assertThat(view.grants().crud()).containsEntry("estoque", List.of("READ"));
        assertThat(view.effective().crud())
                .containsEntry("estoque", List.of("READ"))
                .containsEntry("compras.analise", List.of("READ", "UPDATE"));
        assertThatThrownBy(() -> query.user(UUID.randomUUID())).isInstanceOf(UserNotFoundException.class);
    }
}
