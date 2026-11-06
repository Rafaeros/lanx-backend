package br.lanx.api.engenharia.operacoes.desenhos.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.TransicaoDesenhoInvalidaException;

class DesenhoJobTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC);
    private static final ArquivoEnviado LISTA = new ArquivoEnviado("IT002 KHN830.xlsx", "application/xlsx", 1000);
    private static final ArquivoEnviado PDF = new ArquivoEnviado("KHN830.pdf", "application/pdf", 2000);

    private static DesenhoJob novoJob() {
        return DesenhoJob.solicitar(UUID.randomUUID(), " khn830 ", null, null, LISTA, null, CLOCK);
    }

    private static DesenhoJobArquivo zip(DesenhoJob job) {
        return DesenhoJobArquivo.resultado("desenhos/" + job.getId() + "/resultado.zip", "KHN830.zip", 5000, CLOCK);
    }

    @Test
    @DisplayName("solicitar normaliza o projeto, nasce PENDENTE e gera as chaves dentro da pasta do job")
    void solicitar() {
        DesenhoJob job = DesenhoJob.solicitar(UUID.randomUUID(), " khn830 ", DesenhoOpcoes.padrao(), null, LISTA,
                PDF, CLOCK);

        assertThat(job.getProjeto()).isEqualTo("KHN830");
        assertThat(job.getStatus()).isEqualTo(StatusDesenho.PENDENTE);
        assertThat(job.getTentativas()).isEqualTo(DesenhoJob.PRIMEIRA_TENTATIVA);
        assertThat(job.getArquivos()).extracting(DesenhoJobArquivo::tipo)
                .containsExactly(TipoArquivoDesenho.LISTA_CORTE, TipoArquivoDesenho.DESENHO_CLIENTE);
        assertThat(job.getArquivos()).allSatisfy(
                a -> assertThat(a.chaveArmazenamento()).startsWith("desenhos/" + job.getId() + "/"));
    }

    @Test
    @DisplayName("lista de corte ausente ou que não é .xlsx é recusada")
    void listaCorteObrigatoria() {
        UUID user = UUID.randomUUID();
        assertThatThrownBy(() -> DesenhoJob.solicitar(user, "KHN830", null, null, null, null, CLOCK))
                .isInstanceOf(DesenhoInvalidoException.class).hasMessageContaining("lista de corte");
        var csv = new ArquivoEnviado("lista.csv", "text/csv", 10);
        assertThatThrownBy(() -> DesenhoJob.solicitar(user, "KHN830", null, null, csv, null, CLOCK))
                .isInstanceOf(DesenhoInvalidoException.class);
    }

    @Test
    @DisplayName("projeto que não vira nome de pasta é recusado (o worker o rejeitaria)")
    void projetoInvalido() {
        assertThatThrownBy(() -> DesenhoJob.solicitar(UUID.randomUUID(), "../KHN", null, null, LISTA, null, CLOCK))
                .isInstanceOf(DesenhoInvalidoException.class);
    }

    @Test
    @DisplayName("PENDENTE → PROCESSANDO → CONCLUIDO guarda o zip e as pendências")
    void caminhoFeliz() {
        DesenhoJob job = novoJob();

        job.iniciar(CLOCK.instant());
        job.concluir(zip(job), List.of("Conector X3 sem vista"), CLOCK.instant());

        assertThat(job.getStatus()).isEqualTo(StatusDesenho.CONCLUIDO);
        assertThat(job.zip()).isPresent();
        assertThat(job.getPendencias()).containsExactly("Conector X3 sem vista");
        assertThat(job.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("mensagens repetidas do worker não lançam exceção nem duplicam o zip")
    void idempotente() {
        DesenhoJob job = novoJob();

        job.iniciar(CLOCK.instant());
        job.iniciar(CLOCK.instant());
        job.concluir(zip(job), List.of(), CLOCK.instant());
        job.concluir(zip(job), List.of(), CLOCK.instant());

        assertThat(job.getArquivos()).filteredOn(a -> a.tipo() == TipoArquivoDesenho.RESULTADO_ZIP).hasSize(1);

        DesenhoJob comErro = novoJob();
        comErro.falhar("Lista inválida", CLOCK.instant());
        comErro.falhar("Lista inválida", CLOCK.instant());
        assertThat(comErro.getStatus()).isEqualTo(StatusDesenho.ERRO);
    }

    @Test
    @DisplayName("reprocessar volta para a fila, soma a tentativa e limpa erro e pendências")
    void reprocessar() {
        DesenhoJob job = novoJob();
        job.concluir(zip(job), List.of("aviso antigo"), CLOCK.instant());

        job.reprocessar();

        assertThat(job.getStatus()).isEqualTo(StatusDesenho.PENDENTE);
        assertThat(job.getTentativas()).isEqualTo(2);
        assertThat(job.getPendencias()).isEmpty();
        assertThat(job.getErro()).isNull();
        assertThat(job.zip()).as("o zip anterior fica até o novo chegar").isPresent();
    }

    @Test
    @DisplayName("transição fora do ciclo lança TransicaoDesenhoInvalidaException")
    void transicaoInvalida() {
        DesenhoJob job = novoJob();
        job.concluir(zip(job), List.of(), CLOCK.instant());

        assertThatThrownBy(() -> job.iniciar(CLOCK.instant()))
                .isInstanceOf(TransicaoDesenhoInvalidaException.class)
                .hasMessageContaining("Concluído");
        assertThatThrownBy(() -> novoJob().reprocessar())
                .isInstanceOf(TransicaoDesenhoInvalidaException.class);
    }

    @Test
    @DisplayName("listas do job vindas de fora são copiadas (imutáveis ou com null não quebram)")
    void reconstrucao() {
        DesenhoJob salvo = novoJob();
        Map<String, Object> override = new HashMap<>();
        override.put("drawn_by", null);
        var job = new DesenhoJob(salvo.getId(), salvo.getSolicitanteId(), "KHN830", null, override,
                StatusDesenho.PROCESSANDO, null, null, 1, List.copyOf(salvo.getArquivos()), CLOCK.instant(),
                CLOCK.instant(), null, 3);

        job.concluir(zip(job), List.of(), CLOCK.instant()); // removeIf/add numa lista que era imutável

        assertThat(job.getArquivos()).hasSize(2);
        assertThatThrownBy(() -> job.getArquivos().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("opções: sem formato vira A4 e sem biblioteca não há vistas")
    void opcoes() {
        var o = new DesenhoOpcoes(Set.of(), Acabamento.L, true, false, true);

        assertThat(o.formatos()).containsExactly(FormatoDesenho.A4);
        assertThat(o.vistas()).isFalse();
    }
}
