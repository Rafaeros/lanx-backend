package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.operadores.Operador;
import br.lanx.api.cadastros.tiponaoconformidade.TipoNaoConformidade;
import br.lanx.api.producao.operacoes.ordensdeproducao.OrdemDeProducao;
import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(schema = "operacoes", name = "nao_conformidades")
@SQLDelete(sql = "UPDATE operacoes.nao_conformidades SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class NaoConformidade {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_nao_conformidade_id", nullable = false)
    private TipoNaoConformidade tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_de_producao_id")
    private OrdemDeProducao ordemDeProducao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operador_id")
    private Operador operador;

    @Column(name = "quantidade_nao_conforme", nullable = false)
    private BigDecimal quantidadeNaoConforme;

    @Column(name = "data_nao_conformidade", nullable = false)
    private LocalDate dataNaoConformidade;

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY)
    private List<NaoConformidadeAnexo> anexos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected NaoConformidade() {
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

    public TipoNaoConformidade getTipo() {
        return tipo;
    }

    public void setTipo(TipoNaoConformidade tipo) {
        this.tipo = tipo;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }
    
    public void setOrdemDeProducao(OrdemDeProducao ordemDeProducao) {
        this.ordemDeProducao = ordemDeProducao;
    }

    public OrdemDeProducao getOrdemDeProducao() {
        return ordemDeProducao;
    }

    public Operador getOperador() {
        return operador;
    }

    public void setOperador(Operador operador) {
        this.operador = operador;
    }

    public BigDecimal getQuantidadeNaoConforme() {
        return quantidadeNaoConforme;
    }

    public void setQuantidadeNaoConforme(BigDecimal quantidadeNaoConforme) {
        this.quantidadeNaoConforme = quantidadeNaoConforme;
    }

    public LocalDate getDataNaoConformidade() {
        return dataNaoConformidade;
    }

    public void setDataNaoConformidade(LocalDate dataNaoConformidade) {
        this.dataNaoConformidade = dataNaoConformidade;
    }

    public List<NaoConformidadeAnexo> getAnexos() {
        return anexos;
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
