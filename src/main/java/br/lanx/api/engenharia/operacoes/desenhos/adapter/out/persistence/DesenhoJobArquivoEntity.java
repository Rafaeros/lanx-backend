package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import br.lanx.api.engenharia.operacoes.desenhos.domain.TipoArquivoDesenho;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Linha de {@code engenharia.desenho_job_arquivos}: os anexos e o zip de um job. */
@Entity
@Table(schema = "engenharia", name = "desenho_job_arquivos")
public class DesenhoJobArquivoEntity {

    @Id
    private UUID id;

    /** Dono da FK {@code desenho_job_id}. LAZY: ler um arquivo não precisa carregar o job. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "desenho_job_id", nullable = false, updatable = false)
    private DesenhoJobEntity job;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20, updatable = false)
    private TipoArquivoDesenho tipo;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "chave_armazenamento", nullable = false, length = 1000, unique = true)
    private String chaveArmazenamento;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DesenhoJobArquivoEntity() {
    }

    DesenhoJobArquivoEntity(UUID id, TipoArquivoDesenho tipo, String nomeArquivo, String chaveArmazenamento,
            String contentType, long tamanhoBytes, Instant createdAt) {
        this.id = id;
        this.tipo = tipo;
        this.nomeArquivo = nomeArquivo;
        this.chaveArmazenamento = chaveArmazenamento;
        this.contentType = contentType;
        this.tamanhoBytes = tamanhoBytes;
        this.createdAt = createdAt;
    }

    void setJob(DesenhoJobEntity job) {
        this.job = job;
    }

    public UUID getId() {
        return id;
    }

    public DesenhoJobEntity getJob() {
        return job;
    }

    public TipoArquivoDesenho getTipo() {
        return tipo;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public String getChaveArmazenamento() {
        return chaveArmazenamento;
    }

    public String getContentType() {
        return contentType;
    }

    public long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
