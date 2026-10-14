package br.lanx.api.auth.user;

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

import br.lanx.api.auth.role.Role;
import br.lanx.api.auth.user.exception.InvalidUserException;
import br.lanx.api.auth.user.exception.UserInactiveException;
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
 * Usuário do sistema. As permissões dele são a união das permissões das suas
 * roles.
 */
@Entity
@Table(schema = "auth", name = "users")
@SQLDelete(sql = "UPDATE auth.users SET active = false, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User {

    private static final Pattern USERNAME_FORMAT = Pattern.compile("^[a-z0-9._-]{3,50}$");

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = "auth", name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected User() {
    }

    /**
     * O hash vem pronto: a senha em texto puro é validada e hasheada antes, no
     * service.
     */
    public User(String username, String passwordHash, Set<Role> roles) {
        String normalized = normalizeUsername(username);
        if (!USERNAME_FORMAT.matcher(normalized).matches()) {
            throw new InvalidUserException(
                    "O username deve ter de 3 a 50 caracteres: letras minúsculas, números, . _ -");
        }
        this.username = normalized;
        this.passwordHash = passwordHash;
        replaceRoles(roles);
    }

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = Ids.newId();
        }
    }

    /** Altera roles e situação. Username e senha não mudam por aqui. */
    public void update(Set<Role> roles, boolean active) {
        replaceRoles(roles);
        this.active = active;
    }

    public void ensureCanLogin() {
        if (!active) {
            throw new UserInactiveException();
        }
    }

    /** Permissões efetivas: união das permissões de todas as roles. */
    public Set<String> permissionCodes() {
        Set<String> codes = new HashSet<>();
        roles.forEach(role -> codes.addAll(role.permissionCodes()));
        return codes;
    }

    /**
     * " Joao.Silva " → "joao.silva". Usado também no login: "Joao" e "joao" são o
     * mesmo usuário.
     */
    public static String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private void replaceRoles(Set<Role> newRoles) {
        if (newRoles == null || newRoles.isEmpty()) {
            throw new InvalidUserException("Usuário precisa ter pelo menos uma role");
        }
        this.roles.clear();
        this.roles.addAll(newRoles);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActive() {
        return active;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
