package br.lanx.api.producao.operacoes.ordensdeproducao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import br.lanx.api.cadastros.clientes.Cliente;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(schema = "operacoes", name = "ordens_de_producao")
@SQLDelete(sql = "UPDATE operacoes.ordens_de_producao SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OrdemDeProducao {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    @Column(name = "quantidade", nullable = false)
    private BigDecimal quantidade;

    @Column(name = "previsao", nullable = false)
    private LocalDate previsao;

    /** Etapa em que a OP está (texto livre por enquanto); null = ainda não iniciada. */
    @Column(name = "etapa_atual", length = 100)
    private String etapaAtual;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public OrdemDeProducao() {
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

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDate getPrevisao() {
        return previsao;
    }

    public void setPrevisao(LocalDate previsao) {
        this.previsao = previsao;
    }

    public String getEtapaAtual() {
        return etapaAtual;
    }

    public void setEtapaAtual(String etapaAtual) {
        this.etapaAtual = etapaAtual;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

}
