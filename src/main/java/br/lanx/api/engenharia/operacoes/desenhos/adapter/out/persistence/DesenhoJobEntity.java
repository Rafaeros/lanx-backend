package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Linha de {@code engenharia.desenho_jobs}. Só o formato do banco: as regras ficam em
 * {@link br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob}; o mapper converte entre os dois.
 */
@Entity
@Table(schema = "engenharia", name = "desenho_jobs")
public class DesenhoJobEntity {

    /** Gerado pelo domínio (UUIDv7), por isso sem {@code @GeneratedValue}. */
    @Id
    private UUID id;

    /**
     * Só o id, sem {@code @ManyToOne User}: o módulo de engenharia não deve depender das classes de auth.
     * A FK no banco garante a integridade.
     */
    @Column(name = "solicitante_id", nullable = false, updatable = false)
    private UUID solicitanteId;

    @Column(name = "projeto", nullable = false, length = 100, updatable = false)
    private String projeto;

    /** STRING grava o nome ("PENDENTE"); ORDINAL gravaria 0, 1, 2 e quebraria ao reordenar o enum. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusDesenho status;

    /** JSONB ⇄ record, convertido pelo Hibernate com Jackson. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "opcoes", nullable = false, updatable = false)
    private OpcoesJson opcoes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "project_override", updatable = false)
    private Map<String, Object> projectOverride;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pendencias", nullable = false)
    private List<String> pendencias = new ArrayList<>();

    @Column(name = "erro")
    private String erro;

    @Column(name = "tentativas", nullable = false)
    private int tentativas;

    /**
     * Lock otimista: o UPDATE sai com {@code WHERE version = ?} e incrementa. Se o listener e o "Reprocessar"
     * salvarem o mesmo job ao mesmo tempo, o segundo recebe {@code OptimisticLockException} em vez de
     * sobrescrever o primeiro em silêncio.
     *
     * <p>{@code Long} (e não {@code long}): {@code null} diz ao Spring Data que a entidade é nova (faz
     * {@code persist} direto em vez de {@code merge}, que antes faria um SELECT).
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Os arquivos pertencem ao job: salvar o job salva os arquivos (cascade) e tirar um da lista apaga a
     * linha (orphanRemoval). {@code mappedBy}: quem tem a FK é o lado {@code DesenhoJobArquivoEntity.job}.
     */
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt")
    @BatchSize(size = 50) // no histórico, carrega os arquivos de até 50 jobs numa query só (evita o N+1)
    private List<DesenhoJobArquivoEntity> arquivos = new ArrayList<>();

    /** Vem do domínio (Clock), não do banco: assim o teste controla a data. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Só auditoria: o Hibernate preenche no INSERT e em cada UPDATE. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /** Exigido pelo JPA (cria a instância por reflexão ao ler do banco). */
    protected DesenhoJobEntity() {
    }

    DesenhoJobEntity(UUID id, UUID solicitanteId, String projeto, OpcoesJson opcoes,
            Map<String, Object> projectOverride, Instant createdAt) {
        this.id = id;
        this.solicitanteId = solicitanteId;
        this.projeto = projeto;
        this.opcoes = opcoes;
        this.projectOverride = projectOverride;
        this.createdAt = createdAt;
    }

    /** Mantém os dois lados da relação coerentes (a FK é gravada a partir de {@code arquivo.job}). */
    void adicionarArquivo(DesenhoJobArquivoEntity arquivo) {
        arquivo.setJob(this);
        arquivos.add(arquivo);
    }

    void removerArquivo(DesenhoJobArquivoEntity arquivo) {
        arquivos.remove(arquivo);
        arquivo.setJob(null);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public String getProjeto() {
        return projeto;
    }

    public StatusDesenho getStatus() {
        return status;
    }

    void setStatus(StatusDesenho status) {
        this.status = status;
    }

    public OpcoesJson getOpcoes() {
        return opcoes;
    }

    public Map<String, Object> getProjectOverride() {
        return projectOverride;
    }

    public List<String> getPendencias() {
        return pendencias;
    }

    void setPendencias(List<String> pendencias) {
        this.pendencias = new ArrayList<>(pendencias);
    }

    public String getErro() {
        return erro;
    }

    void setErro(String erro) {
        this.erro = erro;
    }

    public int getTentativas() {
        return tentativas;
    }

    void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    public Long getVersion() {
        return version;
    }

    public List<DesenhoJobArquivoEntity> getArquivos() {
        return arquivos;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
