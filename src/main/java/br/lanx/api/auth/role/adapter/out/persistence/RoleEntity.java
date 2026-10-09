package br.lanx.api.auth.role.adapter.out.persistence;

import br.lanx.api.auth.permission.adapter.out.persistence.FeatureActionEmbeddable;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

@Entity
@Table(schema = "auth", name = "roles")
@SQLDelete(sql = "UPDATE auth.roles SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RoleEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection
    @CollectionTable(schema = "auth", name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @MapKeyColumn(name = "feature")
    @Column(name = "mask", nullable = false)
    private final Map<String, Integer> permissions = new HashMap<>();

    @ElementCollection
    @CollectionTable(schema = "auth", name = "role_actions", joinColumns = @JoinColumn(name = "role_id"))
    private final Set<FeatureActionEmbeddable> actions = new HashSet<>();

    @Transient
    private boolean isNew = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected RoleEntity() {
    }

    /** Usado pelo mapper para uma role NOVA vinda do domínio. */
    RoleEntity(UUID id, String name, Map<String, Integer> permissions, Set<FeatureActionEmbeddable> actions) {
        this.id = id;
        this.name = name;
        this.permissions.putAll(permissions);
        this.actions.addAll(actions);
        this.isNew = true;
    }

    /** Atualiza uma role que já existe, trocando as coleções por inteiro. */
    void update(String name, Map<String, Integer> permissions, Set<FeatureActionEmbeddable> actions) {
        this.name = name;
        this.permissions.clear();
        this.permissions.putAll(permissions);
        this.actions.clear();
        this.actions.addAll(actions);
    }

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

    public String getName() {
        return name;
    }

    public Map<String, Integer> getPermissions() {
        return permissions;
    }

    public Set<FeatureActionEmbeddable> getActions() {
        return actions;
    }
}
