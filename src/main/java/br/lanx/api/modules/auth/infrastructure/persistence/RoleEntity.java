package br.lanx.api.modules.auth.infrastructure.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import br.lanx.api.modules.auth.domain.Feature;

@Entity
@Table(schema = "auth", name = "roles")
public class RoleEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection
    @CollectionTable(schema = "auth", name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "feature")
    @Column(name = "mask", nullable = false)
    private final Map<Feature, Integer> permissions = new HashMap<>();

    protected RoleEntity() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<Feature, Integer> getPermissions() {
        return permissions;
    }
}
