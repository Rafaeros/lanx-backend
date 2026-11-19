package br.lanx.api.rh.cadastros.colaboradores.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.CascadeType;
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
@Table(schema = "recursos_humanos", name = "colaboradores")
@SQLDelete(sql = "UPDATE recursos_humanos.colaboradores SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Colaborador {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColaboradorEmail> emails = new ArrayList<>();

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColaboradorTelefone> telefones = new ArrayList<>();

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColaboradorEndereco> enderecos = new ArrayList<>();

    @Column(length = 255, nullable = false)
    private String nome;

    @Column(name = "cpf_encrypted", nullable = false, length = 80)
    private String cpfEncrypted;

    @Column(name = "cpf_hash", nullable = false, length = 64, unique = true)
    private String cpfHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Colaborador() {
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

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public List<ColaboradorEmail> getEmails() {
        return Collections.unmodifiableList(emails);
    }

    public void adicionarEmail(ColaboradorEmail email) {
        emails.add(email);
        email.setColaborador(this);
    }

    public void removerEmail(ColaboradorEmail email) {
        if (emails.remove(email)) {
            email.setColaborador(null);
        }
    }

    public List<ColaboradorTelefone> getTelefones() {
        return Collections.unmodifiableList(telefones);
    }

    public void adicionarTelefone(ColaboradorTelefone telefone) {
        telefones.add(telefone);
        telefone.setColaborador(this);
    }

    public void removerTelefone(ColaboradorTelefone telefone) {
        if (telefones.remove(telefone)) {
            telefone.setColaborador(null);
        }
    }

    public List<ColaboradorEndereco> getEnderecos() {
        return Collections.unmodifiableList(enderecos);
    }

    public void adicionarEndereco(ColaboradorEndereco endereco) {
        enderecos.add(endereco);
        endereco.setColaborador(this);
    }

    public void removerEndereco(ColaboradorEndereco endereco) {
        if (enderecos.remove(endereco)) {
            endereco.setColaborador(null);
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpfEncrypted() {
        return cpfEncrypted;
    }

    public void setCpfEncrypted(String cpfEncrypted) {
        this.cpfEncrypted = cpfEncrypted;
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public void setCpfHash(String cpfHash) {
        this.cpfHash = cpfHash;
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
