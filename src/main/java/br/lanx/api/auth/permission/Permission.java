package br.lanx.api.auth.permission;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Uma permissão do sistema: "compras.analise:read". A tabela é espelho do
 * catálogo em código (sincronizada na subida): o painel só concede ou retira
 * permissões que existem.
 */
@Entity
@Table(schema = "auth", name = "permissions")
public class Permission {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String code;

    @Column(name = "feature_key", nullable = false, length = 100)
    private String featureKey;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Permission() {
    }

    public Permission(String featureKey, String action, String description) {
        this.featureKey = featureKey;
        this.action = action;
        this.code = codeOf(featureKey, action);
        this.description = description;
    }

    /** ("compras.analise", "read") → "compras.analise:read" */
    public static String codeOf(String featureKey, String action) {
        return featureKey + ":" + action;
    }

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = Ids.newId();
        }
    }

    public void describe(String description) {
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }
}
