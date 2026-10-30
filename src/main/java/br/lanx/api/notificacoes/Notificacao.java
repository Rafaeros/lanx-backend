package br.lanx.api.notificacoes;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(schema = "notificacoes", name = "notificacoes")
public class Notificacao {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacao tipo;

    @Column(length = 150, nullable = false)
    private String titulo;

    @Column(length = 500, nullable = false)
    private String mensagem;

    @Column(length = 255)
    private String link;

    @Column(name = "referencia_id")
    private UUID referenciaId;

    @Column(name = "lida_em")
    private Instant lidaEm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notificacao() {
    }

    @PrePersist
    private void generateId() {
        if (id == null)
            id = Ids.newId();
    }

    public void marcarComoLida() {
        if (lidaEm == null)
            lidaEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public UUID getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(UUID referenciaId) {
        this.referenciaId = referenciaId;
    }

    public Instant getLidaEm() {
        return lidaEm;
    }

    public void setLidaEm(Instant lidaEm) {
        this.lidaEm = lidaEm;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setLink(String linl) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

}
