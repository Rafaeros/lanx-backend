package br.lanx.api.cadastros.pais;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(schema = "cadastros", name = "pais", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pais_codigo_iso2", columnNames = "codigo_iso2"),
        @UniqueConstraint(name = "uk_pais_codigo_iso3", columnNames = "codigo_iso3")
})
public class Pais {

    @Id
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "codigo_iso2", nullable = false, length = 2)
    private String codigoIso2;

    @Column(name = "codigo_iso3", nullable = false, length = 3)
    private String codigoIso3;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Pais() {
    }

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = Ids.newId();
        }
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

    public String getCodigoIso2() {
        return codigoIso2;
    }

    public void setCodigoIso2(String codigoIso2) {
        this.codigoIso2 = codigoIso2;
    }

    public String getCodigoIso3() {
        return codigoIso3;
    }

    public void setCodigoIso3(String codigoIso3) {
        this.codigoIso3 = codigoIso3;
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

}
