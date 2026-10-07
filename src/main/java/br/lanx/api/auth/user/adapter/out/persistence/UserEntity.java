package br.lanx.api.auth.user.adapter.out.persistence;

import br.lanx.api.auth.permission.adapter.out.persistence.FeatureActionEmbeddable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(schema = "auth", name = "users")
public class UserEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active;

    // referência por id: a role é outro agregado (módulo role); a chave estrangeira fica no SQL
    @ElementCollection
    @CollectionTable(schema = "auth", name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_id", nullable = false)
    private Set<UUID> roleIds = new HashSet<>();

    // ---------- exceções individuais: grants somam à role, denies subtraem ----------

    @ElementCollection
    @CollectionTable(schema = "auth", name = "user_grants", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "feature")
    @Column(name = "mask", nullable = false)
    private Map<String, Integer> grantMasks = new HashMap<>();

    @ElementCollection
    @CollectionTable(schema = "auth", name = "user_denies", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "feature")
    @Column(name = "mask", nullable = false)
    private Map<String, Integer> denyMasks = new HashMap<>();

    @ElementCollection
    @CollectionTable(schema = "auth", name = "user_grant_actions", joinColumns = @JoinColumn(name = "user_id"))
    private Set<FeatureActionEmbeddable> grantActions = new HashSet<>();

    @ElementCollection
    @CollectionTable(schema = "auth", name = "user_deny_actions", joinColumns = @JoinColumn(name = "user_id"))
    private Set<FeatureActionEmbeddable> denyActions = new HashSet<>();

    @Transient
    private boolean isNew = false;

    protected UserEntity() {
    } // usado pelo Hibernate ao carregar

    /** Usado pelo mapper para um usuário NOVO vindo do domínio. */
    UserEntity(UUID id, String username, String passwordHash, boolean active, Set<UUID> roleIds,
            Map<String, Integer> grantMasks, Set<FeatureActionEmbeddable> grantActions,
            Map<String, Integer> denyMasks, Set<FeatureActionEmbeddable> denyActions) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
        this.roleIds.addAll(roleIds);
        this.grantMasks.putAll(grantMasks);
        this.grantActions.addAll(grantActions);
        this.denyMasks.putAll(denyMasks);
        this.denyActions.addAll(denyActions);
        this.isNew = true;
    }

    /** Atualiza um usuário que já existe. Username e senha não mudam por aqui. */
    void update(boolean active, Set<UUID> roleIds,
            Map<String, Integer> grantMasks, Set<FeatureActionEmbeddable> grantActions,
            Map<String, Integer> denyMasks, Set<FeatureActionEmbeddable> denyActions) {
        this.active = active;
        this.roleIds.clear();
        this.roleIds.addAll(roleIds);
        this.grantMasks.clear();
        this.grantMasks.putAll(grantMasks);
        this.grantActions.clear();
        this.grantActions.addAll(grantActions);
        this.denyMasks.clear();
        this.denyMasks.putAll(denyMasks);
        this.denyActions.clear();
        this.denyActions.addAll(denyActions);
    }

    // ---------- Persistable: diz ao Spring Data se é INSERT ou UPDATE ----------

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    // ---------- getters ----------

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public Set<UUID> getRoleIds() {
        return roleIds;
    }

    public Map<String, Integer> getGrantMasks() {
        return grantMasks;
    }

    public Map<String, Integer> getDenyMasks() {
        return denyMasks;
    }

    public Set<FeatureActionEmbeddable> getGrantActions() {
        return grantActions;
    }

    public Set<FeatureActionEmbeddable> getDenyActions() {
        return denyActions;
    }
}
