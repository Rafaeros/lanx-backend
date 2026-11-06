package br.lanx.api.engenharia.operacoes.desenhos.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.TransicaoDesenhoInvalidaException;
import br.lanx.api.shared.domain.Ids;

/**
 * Pedido de geração dos desenhos de um chicote (IT001 A4 e/ou painel 1:1) e o seu ciclo de vida.
 *
 * <p>O job nasce {@link StatusDesenho#PENDENTE}; o worker avisa {@code PROCESSANDO} e depois
 * {@code CONCLUIDO} (com o zip e as pendências) ou {@code ERRO}. O RabbitMQ pode entregar a mesma mensagem
 * mais de uma vez (ex.: o worker reiniciou no meio do job), por isso {@link #iniciar}, {@link #concluir} e
 * {@link #falhar} são idempotentes: repetir o mesmo evento não muda nada nem lança exceção.
 */
public final class DesenhoJob {

    /** Mesma regra do worker: o projeto vira nome de pasta, então não aceita "/" nem "..". */
    private static final Pattern PROJETO = Pattern.compile("^[\\w.\\- ]{1,100}$");
    public static final int PRIMEIRA_TENTATIVA = 1;

    private final UUID id;
    private final UUID solicitanteId;
    private final String projeto;
    private final DesenhoOpcoes opcoes;
    private final Map<String, Object> projectOverride;
    private StatusDesenho status;
    private List<String> pendencias;
    private String erro;
    private int tentativas;
    private final List<DesenhoJobArquivo> arquivos;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private final long version;

    /**
     * Cria o pedido com os arquivos enviados pelo usuário. A chave de cada arquivo já sai pronta: o service
     * grava os bytes no storage com {@code arquivo.chaveArmazenamento()}.
     *
     * @param override campos do project.json revisados no front (carimbo, topologia); pode ser {@code null}
     * @param desenhoCliente PDF do cliente, opcional
     */
    public static DesenhoJob solicitar(UUID solicitanteId, String projeto, DesenhoOpcoes opcoes,
            Map<String, Object> override, ArquivoEnviado listaCorte, ArquivoEnviado desenhoCliente, Clock clock) {
        Objects.requireNonNull(solicitanteId, "solicitanteId");
        String nome = projeto == null ? "" : projeto.trim().toUpperCase();
        if (!PROJETO.matcher(nome).matches())
            throw new DesenhoInvalidoException(
                    "Código de projeto inválido: use letras, números, espaço, \".\", \"-\" ou \"_\" (até 100)");
        exigirExtensao(listaCorte, ".xlsx", "A lista de corte (.xlsx) é obrigatória");
        if (desenhoCliente != null)
            exigirExtensao(desenhoCliente, ".pdf", "O desenho do cliente deve ser um PDF");

        UUID id = Ids.newId();
        var arquivos = new ArrayList<DesenhoJobArquivo>();
        arquivos.add(DesenhoJobArquivo.enviado(id, TipoArquivoDesenho.LISTA_CORTE, listaCorte, clock));
        if (desenhoCliente != null)
            arquivos.add(DesenhoJobArquivo.enviado(id, TipoArquivoDesenho.DESENHO_CLIENTE, desenhoCliente, clock));

        return new DesenhoJob(id, solicitanteId, nome, opcoes == null ? DesenhoOpcoes.padrao() : opcoes, override,
                StatusDesenho.PENDENTE, List.of(), null, PRIMEIRA_TENTATIVA, arquivos, clock.instant(), null, null,
                0L);
    }

    private static void exigirExtensao(ArquivoEnviado arquivo, String extensao, String mensagem) {
        if (arquivo == null || arquivo.nome() == null || !arquivo.nome().toLowerCase().endsWith(extensao))
            throw new DesenhoInvalidoException(mensagem);
    }

    /** Reconstrói um job já salvo (usado pelo mapper da persistência); não valida transições. */
    public DesenhoJob(UUID id, UUID solicitanteId, String projeto, DesenhoOpcoes opcoes,
            Map<String, Object> projectOverride, StatusDesenho status, List<String> pendencias, String erro,
            int tentativas, List<DesenhoJobArquivo> arquivos, Instant createdAt, Instant startedAt,
            Instant completedAt, long version) {
        this.id = Objects.requireNonNull(id, "id");
        this.solicitanteId = Objects.requireNonNull(solicitanteId, "solicitanteId");
        this.projeto = Objects.requireNonNull(projeto, "projeto");
        this.opcoes = opcoes == null ? DesenhoOpcoes.padrao() : opcoes;
        // cópias: o que vem de fora (JPA, request) pode ser imutável ou mudar depois
        // LinkedHashMap e não Map.copyOf: o JSON revisado pode ter valores null
        this.projectOverride = projectOverride == null ? null
                : Collections.unmodifiableMap(new LinkedHashMap<>(projectOverride));
        this.status = Objects.requireNonNull(status, "status");
        this.pendencias = pendencias == null ? List.of() : List.copyOf(pendencias);
        this.erro = erro;
        this.tentativas = tentativas;
        this.arquivos = arquivos == null ? new ArrayList<>() : new ArrayList<>(arquivos);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.version = version;
    }

    /** O worker pegou o job. */
    public void iniciar(Instant agora) {
        if (status == StatusDesenho.PROCESSANDO)
            return; // reentrega: o worker reiniciou e avisou de novo
        transicionarPara(StatusDesenho.PROCESSANDO);
        startedAt = agora;
    }

    /** O worker gerou o zip; substitui o zip de uma tentativa anterior, se houver. */
    public void concluir(DesenhoJobArquivo zip, List<String> pendencias, Instant agora) {
        if (status == StatusDesenho.CONCLUIDO)
            return; // reentrega
        if (zip == null || zip.tipo() != TipoArquivoDesenho.RESULTADO_ZIP)
            throw new DesenhoInvalidoException("O resultado precisa do zip gerado");
        transicionarPara(StatusDesenho.CONCLUIDO);
        arquivos.removeIf(a -> a.tipo() == TipoArquivoDesenho.RESULTADO_ZIP);
        arquivos.add(zip);
        this.pendencias = pendencias == null ? List.of() : List.copyOf(pendencias);
        this.erro = null;
        completedAt = agora;
    }

    /** O worker não conseguiu gerar; {@code erro} é a mensagem para o usuário. */
    public void falhar(String erro, Instant agora) {
        if (status == StatusDesenho.ERRO)
            return; // reentrega
        transicionarPara(StatusDesenho.ERRO);
        this.erro = erro;
        completedAt = agora;
    }

    /** Volta para a fila com o mesmo id (os mesmos arquivos de entrada); o zip anterior fica até o novo chegar. */
    public void reprocessar() {
        transicionarPara(StatusDesenho.PENDENTE);
        tentativas++;
        erro = null;
        pendencias = List.of();
        startedAt = null;
        completedAt = null;
    }

    private void transicionarPara(StatusDesenho destino) {
        if (!status.podeIrPara(destino))
            throw new TransicaoDesenhoInvalidaException(id, status, destino);
        status = destino;
    }

    public Optional<DesenhoJobArquivo> arquivo(TipoArquivoDesenho tipo) {
        return arquivos.stream().filter(a -> a.tipo() == tipo).findFirst();
    }

    public Optional<DesenhoJobArquivo> zip() {
        return arquivo(TipoArquivoDesenho.RESULTADO_ZIP);
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

    public DesenhoOpcoes getOpcoes() {
        return opcoes;
    }

    public Map<String, Object> getProjectOverride() {
        return projectOverride;
    }

    public StatusDesenho getStatus() {
        return status;
    }

    public List<String> getPendencias() {
        return pendencias;
    }

    public String getErro() {
        return erro;
    }

    public int getTentativas() {
        return tentativas;
    }

    /** Cópia: alterar a lista devolvida não altera o job (só os métodos acima mudam o estado). */
    public List<DesenhoJobArquivo> getArquivos() {
        return List.copyOf(arquivos);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public long getVersion() {
        return version;
    }
}
