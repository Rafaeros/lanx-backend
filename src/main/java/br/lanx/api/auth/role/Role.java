package br.lanx.api.auth.role;

import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import br.lanx.api.auth.permission.Permission;
import br.lanx.api.auth.role.exception.InvalidRoleException;
import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Perfil de acesso. O nome é o identificador funcional: MAIÚSCULO com "_"
 * (ADMIN, GERENTE_COMPRAS). As permissões são linhas da tabela permissions.
 */
@Entity
@Table(schema = "auth", name = "roles")
@SQLDelete(sql = "UPDATE auth.roles SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Role {

    public static final String ADMIN = "ADMIN";

    private static final Pattern NAME_FORMAT = Pattern.compile("^[A-Z0-9_]{3,50}$");

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = "auth", name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Role() {
    }

    public Role(String name, String description, Set<Permission> permissions) {
        this.name = validatedName(name);
        this.description = description;
        replacePermissions(permissions);
    }

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = Ids.newId();
        }
    }

    public void update(String name, String description, Set<Permission> permissions) {
        this.name = validatedName(name);
        this.description = description;
        replacePermissions(permissions);
    }

    /**
     * Usado pela sincronização: a ADMIN sempre recebe tudo, sem revalidar o nome.
     */
    public void grantOnly(Set<Permission> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    public Set<String> permissionCodes() {
        Set<String> codes = new HashSet<>();
        permissions.forEach(p -> codes.add(p.getCode()));
        return codes;
    }

    /**
     * " comprador " → "COMPRADOR": só trim e maiúsculas; o formato é validado em
     * seguida.
     */
    public static String normalizeName(String name) {
        return name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
    }

    private static String validatedName(String name) {
        String normalized = normalizeName(name);
        if (!NAME_FORMAT.matcher(normalized).matches()) {
            throw new InvalidRoleException(
                    "O nome da role deve ter de 3 a 50 caracteres: letras, números ou _ (ex.: GERENTE_COMPRAS)");
        }
        return normalized;
    }

    private void replacePermissions(Set<Permission> newPermissions) {
        if (newPermissions == null || newPermissions.isEmpty()) {
            throw new InvalidRoleException("Role precisa ter pelo menos uma permissão");
        }
        this.permissions.clear();
        this.permissions.addAll(newPermissions);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
