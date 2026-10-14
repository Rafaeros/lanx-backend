package br.lanx.api.cadastros.unidade_medida.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "cadastros", name = "unidades_medida")
@SQLDelete(sql = "UPDATE cadastros.unidades_medida SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UnidadeMedidaEntity {
    @Id
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Column(name = "sigla", unique = true, nullable = false, length = 10)
    private String sigla;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected UnidadeMedidaEntity() {
    }

    UnidadeMedidaEntity(UUID id, String nome, String sigla) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

}
