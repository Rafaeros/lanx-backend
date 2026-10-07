package br.lanx.api.auth.role.application.service;

import br.lanx.api.auth.permission.domain.PermissionSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.auth.role.application.port.in.GetRolesUseCase.RoleView;
import br.lanx.api.auth.permission.application.port.in.PermissionsInput;
import br.lanx.api.auth.role.application.port.in.RegisterRoleUseCase.RegisterRoleCommand;
import br.lanx.api.auth.role.application.port.in.UpdateRoleUseCase.UpdateRoleCommand;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.FeatureCatalog;
import br.lanx.api.auth.permission.domain.exception.InvalidPermissionException;
import br.lanx.api.auth.role.domain.exception.RoleAlreadyExistsException;
import br.lanx.api.auth.role.domain.exception.RoleNotFoundException;
import br.lanx.api.auth.permission.domain.exception.UnknownFeatureException;
import br.lanx.api.auth.fakes.InMemoryPermissionCache;
import br.lanx.api.auth.fakes.InMemoryRoleRepository;
import br.lanx.api.shared.authorization.FeatureDefinition;
import br.lanx.api.shared.authorization.FeatureKey;

class RoleServicesTest {

    private InMemoryRoleRepository roles;
    private InMemoryPermissionCache cache;
    private RegisterRoleService register;
    private UpdateRoleService update;
    private RoleQueryService query;

    @BeforeEach
    void setUp() {
        roles = new InMemoryRoleRepository();
        cache = new InMemoryPermissionCache();
        var catalog = FeatureCatalog.of(List.of(
                FeatureDefinition.of("compras", "Compras"),
                FeatureDefinition.of("compras.analise", "Análise", "aprovar")));
        register = new RegisterRoleService(roles, catalog);
        update = new UpdateRoleService(roles, catalog, cache);
        query = new RoleQueryService(roles);
    }

    private static PermissionsInput analise(String... crud) {
        return new PermissionsInput(Map.of("compras.analise", Set.of(crud)), Map.of("compras.analise", Set.of("aprovar")));
    }

    @Test
    @DisplayName("cria a role convertendo nomes em bitmask")
    void cria() {
        CreatedResult result = register.register(new RegisterRoleCommand(" comprador ", analise("read", "UPDATE")));

        var saved = roles.findById(result.id()).orElseThrow();
        assertThat(saved.name()).isEqualTo("COMPRADOR");
        assertThat(saved.permissions().masks().get(FeatureKey.of("compras.analise")))
                .isEqualTo(Action.maskOf(Action.READ, Action.UPDATE));
        assertThat(saved.permissions().canDo(FeatureKey.of("compras.analise"), "aprovar")).isTrue();
    }

    @Test
    @DisplayName("nome repetido → conflito")
    void duplicada() {
        register.register(new RegisterRoleCommand("COMPRADOR", analise("READ")));

        assertThatThrownBy(() -> register.register(new RegisterRoleCommand("comprador", analise("READ"))))
                .isInstanceOf(RoleAlreadyExistsException.class);
    }

    @Test
    @DisplayName("funcionalidade ou ação fora do catálogo, e ação CRUD inválida, são rejeitadas")
    void invalidas() {
        assertThatThrownBy(() -> register.register(new RegisterRoleCommand("RH",
                new PermissionsInput(Map.of("rh", Set.of("READ")), null))))
                .isInstanceOf(UnknownFeatureException.class);
        assertThatThrownBy(() -> register.register(new RegisterRoleCommand("RH",
                new PermissionsInput(null, Map.of("compras.analise", Set.of("exportar"))))))
                .isInstanceOf(UnknownFeatureException.class);
        assertThatThrownBy(() -> register.register(new RegisterRoleCommand("RH", analise("LER"))))
                .isInstanceOf(InvalidPermissionException.class);
    }

    @Test
    @DisplayName("edita permissões, mantém o id e invalida o cache")
    void edita() {
        UUID id = register.register(new RegisterRoleCommand("COMPRADOR", analise("READ"))).id();
        cache.put(UUID.randomUUID(), br.lanx.api.auth.permission.domain.PermissionSet.EMPTY);

        update.update(new UpdateRoleCommand(id, "COMPRADOR_SENIOR", analise("READ", "DELETE")));

        var edited = roles.findById(id).orElseThrow();
        assertThat(edited.name()).isEqualTo("COMPRADOR_SENIOR");
        assertThat(edited.allows(FeatureKey.of("compras.analise"), Action.DELETE)).isTrue();
        assertThat(cache.get(id)).isEmpty();
        assertThat(roles.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("renomear para um nome que já existe → conflito; role inexistente → não encontrada")
    void erros() {
        register.register(new RegisterRoleCommand("COMPRADOR", analise("READ")));
        UUID outra = register.register(new RegisterRoleCommand("GERENTE", analise("READ"))).id();

        assertThatThrownBy(() -> update.update(new UpdateRoleCommand(outra, "COMPRADOR", analise("READ"))))
                .isInstanceOf(RoleAlreadyExistsException.class);
        assertThatThrownBy(() -> update.update(new UpdateRoleCommand(UUID.randomUUID(), "X_Y_Z", analise("READ"))))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    @DisplayName("consulta devolve as permissões por nome, prontas para pré-marcar checkboxes")
    void consulta() {
        UUID id = register.register(new RegisterRoleCommand("COMPRADOR", analise("UPDATE", "READ"))).id();

        RoleView view = query.role(id);

        assertThat(view.permissions().crud()).containsEntry("compras.analise", List.of("READ", "UPDATE"));
        assertThat(view.permissions().actions()).containsEntry("compras.analise", List.of("aprovar"));
        assertThat(query.roles()).hasSize(1);
        assertThatThrownBy(() -> query.role(UUID.randomUUID())).isInstanceOf(RoleNotFoundException.class);
    }
}
