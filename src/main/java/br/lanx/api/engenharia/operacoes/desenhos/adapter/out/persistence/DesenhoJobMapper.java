package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoDetalhe;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoResumo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.Acabamento;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJobArquivo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoOpcoes;
import br.lanx.api.engenharia.operacoes.desenhos.domain.FormatoDesenho;
import br.lanx.api.engenharia.operacoes.desenhos.domain.TipoArquivoDesenho;

/**
 * Converte entre o banco ({@link DesenhoJobEntity}) e o domínio ({@link DesenhoJob}) e monta os read models.
 * Não decide nada: só copia e traduz formatos.
 */
@Component
class DesenhoJobMapper {

    // ============================== banco → domínio ==============================

    /** Usado ao carregar um job para aplicar uma regra (iniciar, concluir, reprocessar...). */
    DesenhoJob toDomain(DesenhoJobEntity e) {
        return new DesenhoJob(
                e.getId(),
                e.getSolicitanteId(),
                e.getProjeto(),
                toOpcoes(e.getOpcoes()), // OpcoesJson (banco) → DesenhoOpcoes (domínio)
                e.getProjectOverride(),
                e.getStatus(),
                e.getPendencias(),
                e.getErro(),
                e.getTentativas(),
                e.getArquivos().stream().map(this::toArquivo).toList(), // Entity → record do domínio
                e.getCreatedAt(),
                e.getStartedAt(),
                e.getCompletedAt(),
                e.getVersion() == null ? 0L : e.getVersion()); // Long (banco) → long (domínio)
    }

    // ============================== domínio → banco ==============================

    /** INSERT: job recém-solicitado, ainda não existe no banco. */
    DesenhoJobEntity toNewEntity(DesenhoJob d) {
        var e = new DesenhoJobEntity(d.getId(), d.getSolicitanteId(), d.getProjeto(), toJson(d.getOpcoes()),
                d.getProjectOverride(), d.getCreatedAt());
        copyState(d, e);
        addNewFiles(d, e);
        return e;
    }

    /**
     * UPDATE: copia para a Entity já carregada só o que muda no ciclo de vida (projeto, opções e solicitante
     * são {@code updatable = false}). Remove os arquivos que saíram do domínio, mas NÃO adiciona os novos:
     * o adapter dá um flush entre as duas coisas (ver {@link #addNewFiles}).
     */
    void copyState(DesenhoJob d, DesenhoJobEntity e) {
        e.setStatus(d.getStatus());
        e.setPendencias(d.getPendencias());
        e.setErro(d.getErro());
        e.setTentativas(d.getTentativas());
        e.setStartedAt(d.getStartedAt());
        e.setCompletedAt(d.getCompletedAt());
        removeDeletedFiles(d, e);
    }

    /**
     * Adiciona os arquivos que estão no domínio e ainda não na Entity (ex.: o zip novo). Chamado depois do
     * flush: o Hibernate executa INSERT antes de DELETE, e o zip novo bateria no
     * {@code UNIQUE (desenho_job_id, tipo)} com o antigo ainda lá.
     */
    void addNewFiles(DesenhoJob d, DesenhoJobEntity e) {
        Set<UUID> jaGravados = e.getArquivos().stream().map(DesenhoJobArquivoEntity::getId)
                .collect(Collectors.toSet());
        d.getArquivos().stream()
                .filter(a -> !jaGravados.contains(a.id()))
                .map(this::toArquivoEntity)
                .forEach(e::adicionarArquivo);
    }

    private void removeDeletedFiles(DesenhoJob d, DesenhoJobEntity e) {
        Set<UUID> noDominio = d.getArquivos().stream().map(DesenhoJobArquivo::id).collect(Collectors.toSet());
        // List.copyOf: remover da lista enquanto se percorre ela mesma dá ConcurrentModificationException
        List.copyOf(e.getArquivos()).stream()
                .filter(a -> !noDominio.contains(a.getId()))
                .forEach(e::removerArquivo); // orphanRemoval = true → DELETE no flush
    }

    // ============================== read models (consultas) ==============================

    DesenhoDetalhe toDetalhe(DesenhoJobEntity e) {
        return new DesenhoDetalhe(
                e.getId(),
                e.getSolicitanteId(),
                e.getProjeto(),
                e.getStatus(),
                toOpcoes(e.getOpcoes()),
                List.copyOf(e.getPendencias()),
                e.getErro(),
                e.getTentativas(),
                e.getArquivos().stream()
                        .map(a -> new DesenhoDetalhe.Arquivo(a.getId(), a.getTipo(), a.getNomeArquivo(),
                                a.getTamanhoBytes()))
                        .toList(),
                e.getCreatedAt(),
                e.getStartedAt(),
                e.getCompletedAt());
    }

    DesenhoResumo toResumo(DesenhoJobEntity e) {
        boolean temZip = e.getArquivos().stream().anyMatch(a -> a.getTipo() == TipoArquivoDesenho.RESULTADO_ZIP);
        return new DesenhoResumo(
                e.getId(),
                e.getProjeto(),
                e.getStatus(),
                e.getTentativas(),
                e.getPendencias().size(),
                temZip,
                e.getCreatedAt(),
                e.getCompletedAt());
    }

    // ============================== peças ==============================

    private DesenhoJobArquivo toArquivo(DesenhoJobArquivoEntity a) {
        return new DesenhoJobArquivo(a.getId(), a.getTipo(), a.getNomeArquivo(), a.getChaveArmazenamento(),
                a.getContentType(), a.getTamanhoBytes(), a.getCreatedAt());
    }

    private DesenhoJobArquivoEntity toArquivoEntity(DesenhoJobArquivo a) {
        return new DesenhoJobArquivoEntity(a.id(), a.tipo(), a.nomeArquivo(), a.chaveArmazenamento(),
                a.contentType(), a.tamanhoBytes(), a.createdAt());
    }

    /** JSON do banco → opções do domínio: {@code "a4"} → {@code A4}; {@code "F"} → {@code Acabamento.F}. */
    private DesenhoOpcoes toOpcoes(OpcoesJson j) {
        if (j == null)
            return DesenhoOpcoes.padrao();
        Set<FormatoDesenho> formatos = j.formatos() == null ? Set.of()
                : j.formatos().stream().map(FormatoDesenho::deFlag).collect(Collectors.toSet());
        Acabamento acabamento = j.acabamento() == null ? null : Acabamento.valueOf(j.acabamento());
        return new DesenhoOpcoes(formatos, acabamento, j.vistas(), j.biblioteca(), j.dwg());
    }

    /** Opções do domínio → JSON do banco (mesmo formato do worker: {@code "a4"}, {@code "F"}). */
    private OpcoesJson toJson(DesenhoOpcoes o) {
        return new OpcoesJson(
                o.formatos().stream().map(FormatoDesenho::flag).sorted().toList(), // sorted: JSON estável
                o.acabamento() == null ? null : o.acabamento().name(),
                o.vistas(),
                o.biblioteca(),
                o.dwg());
    }
}
