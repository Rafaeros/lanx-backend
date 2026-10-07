package br.lanx.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.auth.role.application.port.out.RoleRepository;
import br.lanx.api.auth.user.application.port.out.UserRepository;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.permission.domain.PermissionSet;
import br.lanx.api.auth.role.domain.Role;
import br.lanx.api.auth.user.domain.User;
import br.lanx.api.shared.authorization.FeatureKey;
import jakarta.persistence.EntityManager;

/** Grava pelos ports, limpa o cache do Hibernate e lê de volta: prova o mapeamento. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersistenceRoundTripTest {

    private static final FeatureKey ANALISE = FeatureKey.of("compras.analise");
    private static final FeatureKey FORNECEDORES = FeatureKey.of("compras.cadastros.fornecedores");

    @Autowired
    RoleRepository roles;
    @Autowired
    UserRepository users;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("role guarda máscaras e ações extras")
    void role() {
        Role saved = roles.save(Role.create("COMPRADOR", new PermissionSet(
                Map.of(ANALISE, Action.maskOf(Action.READ, Action.UPDATE)),
                Map.of(ANALISE, Set.of("aprovar")))));
        em.flush();
        em.clear();

        Role loaded = roles.findByName("COMPRADOR").orElseThrow();

        assertThat(loaded.id()).isEqualTo(saved.id());
        assertThat(loaded.permissions()).isEqualTo(saved.permissions());
        assertThat(roles.existsByName("COMPRADOR")).isTrue();
    }

    @Test
    @DisplayName("user guarda roles, grants e denies")
    void user() {
        Role role = roles.save(Role.create("GERENTE", new PermissionSet(
                Map.of(ANALISE, Action.maskOf(Action.READ, Action.UPDATE)),
                Map.of(ANALISE, Set.of("aprovar", "exportar")))));

        User created = User.create("maria", "hash", Set.of(role.id()));
        User withOverrides = new User(created.id(), created.username(), created.passwordHash(), true,
                created.roleIds(),
                new PermissionSet(Map.of(FORNECEDORES, Action.maskOf(Action.READ)),
                        Map.of(FORNECEDORES, Set.of("anexar-arquivo"))),
                new PermissionSet(Map.of(ANALISE, Action.maskOf(Action.UPDATE)),
                        Map.of(ANALISE, Set.of("exportar"))));
        users.save(withOverrides);
        em.flush();
        em.clear();

        User byName = users.findByUsername("maria").orElseThrow();
        User byId = users.findById(created.id()).orElseThrow();

        assertThat(byName.grants()).isEqualTo(withOverrides.grants());
        assertThat(byName.denies()).isEqualTo(withOverrides.denies());
        assertThat(byName.effectivePermissions(List.of(role))).isEqualTo(withOverrides.effectivePermissions(List.of(role)));
        assertThat(byId.effectivePermissions(List.of(role)).authorities()).containsExactlyInAnyOrder(
                "compras.analise:READ", "compras.analise:aprovar",
                "compras.cadastros.fornecedores:READ", "compras.cadastros.fornecedores:anexar-arquivo");
    }
}
