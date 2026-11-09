package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.lanx.api.engenharia.operacoes.desenhos.application.event.DesenhoExcluidoEvent;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.ArquivoEnviado;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJobArquivo;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoJobNotFoundException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoNaoPodeSerDeletadoException;

class DeleteDesenhoServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC);
    private static final ArquivoEnviado LISTA = new ArquivoEnviado("IT002 KHN830.xlsx", "application/xlsx", 1000);

    private final Map<UUID, DesenhoJob> banco = new HashMap<>();
    private final List<UUID> deletados = new ArrayList<>();
    private final List<Object> eventos = new ArrayList<>();

    private final DesenhoJobRepository jobs = new DesenhoJobRepository() {
        public DesenhoJob save(DesenhoJob job) {
            throw new UnsupportedOperationException();
        }

        public Optional<DesenhoJob> findById(UUID id) {
            return Optional.ofNullable(banco.get(id));
        }

        public void deleteById(UUID id) {
            deletados.add(id);
        }
    };

    private final DeleteDesenhoService service = new DeleteDesenhoService(jobs, eventos::add);

    private DesenhoJob jobPendente() {
        DesenhoJob job = DesenhoJob.solicitar(UUID.randomUUID(), "KHN830", null, null, LISTA, null, CLOCK);
        banco.put(job.getId(), job);
        return job;
    }

    private DesenhoJob jobConcluido() {
        DesenhoJob job = jobPendente();
        job.concluir(DesenhoJobArquivo.resultado("desenhos/" + job.getId() + "/resultado.zip", "KHN830.zip", 5000,
                CLOCK), List.of(), CLOCK.instant());
        return job;
    }

    @Test
    void desenho_concluido_e_apagado_e_avisa_todas_as_chaves() {
        DesenhoJob job = jobConcluido();
        List<String> chaves = job.getArquivos().stream().map(DesenhoJobArquivo::chaveArmazenamento).toList();

        service.deleteById(job.getId());

        assertThat(deletados).containsExactly(job.getId());
        assertThat(chaves).hasSize(2); // lista de corte e zip
        assertThat(eventos).containsExactly(new DesenhoExcluidoEvent(chaves));
    }

    @Test
    void desenho_com_erro_pode_ser_apagado() {
        DesenhoJob job = jobPendente();
        job.falhar("Falha no worker", CLOCK.instant());

        service.deleteById(job.getId());

        assertThat(deletados).containsExactly(job.getId());
        assertThat(eventos).hasSize(1);
    }

    @Test
    void desenho_pendente_nao_pode_ser_apagado() {
        DesenhoJob job = jobPendente();

        assertThatThrownBy(() -> service.deleteById(job.getId()))
                .isInstanceOf(DesenhoNaoPodeSerDeletadoException.class);
        assertThat(deletados).isEmpty();
        assertThat(eventos).isEmpty();
    }

    @Test
    void desenho_processando_nao_pode_ser_apagado() {
        DesenhoJob job = jobPendente();
        job.iniciar(CLOCK.instant());

        assertThatThrownBy(() -> service.deleteById(job.getId()))
                .isInstanceOf(DesenhoNaoPodeSerDeletadoException.class);
        assertThat(deletados).isEmpty();
        assertThat(eventos).isEmpty();
    }

    @Test
    void desenho_inexistente_devolve_nao_encontrado() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.deleteById(id)).isInstanceOf(DesenhoJobNotFoundException.class);
        assertThat(deletados).isEmpty();
        assertThat(eventos).isEmpty();
    }
}
