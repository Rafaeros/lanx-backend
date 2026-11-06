package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase.SolicitarDesenhoCommand;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase.Upload;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.DesenhoJobRepository;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoOpcoes;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.CaboListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.PontaConector;
import br.lanx.api.shared.storage.AnexoStoragePort;

class SolicitarDesenhoServiceTest {

    private final List<DesenhoJob> salvos = new ArrayList<>();
    private final List<String> gravados = new ArrayList<>();
    private final List<Object> eventos = new ArrayList<>();
    private List<CaboListaCorte> cabosDaPlanilha = List.of();

    private final DesenhoJobRepository jobs = new DesenhoJobRepository() {
        public DesenhoJob save(DesenhoJob job) {
            salvos.add(job);
            return job;
        }

        public Optional<DesenhoJob> findById(UUID id) {
            return Optional.empty();
        }
    };

    private final AnexoStoragePort storage = new AnexoStoragePort() {
        public void salvar(String chave, InputStream conteudo) {
            gravados.add(chave);
        }

        public InputStream carregar(String chave) {
            throw new UnsupportedOperationException();
        }

        public void remover(String chave) {
            gravados.remove(chave);
        }
    };

    private final SolicitarDesenhoService service = new SolicitarDesenhoService(jobs, storage, eventos::add,
            new AnalisarListaCorteService(xlsx -> cabosDaPlanilha),
            Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC));

    private static SolicitarDesenhoCommand pedido() {
        var lista = new Upload("IT002 KHN830.xlsx", "application/xlsx", 10,
                () -> new ByteArrayInputStream(new byte[10]));
        return new SolicitarDesenhoCommand(UUID.randomUUID(), "KHN830", DesenhoOpcoes.padrao(), null, lista, null);
    }

    private static CaboListaCorte cabo(String circ, String cavC1) {
        return new CaboListaCorte(circ, "IT CABOS", 7, new PontaConector("CNFAT04CZ", "C1", cavC1), null);
    }

    @Test
    void lista_com_cavidade_duplicada_nao_vai_para_a_fila() {
        cabosDaPlanilha = List.of(cabo("W01", "1"), cabo("W02", "1"));

        assertThatThrownBy(() -> service.solicitar(pedido()))
                .isInstanceOf(DesenhoInvalidoException.class)
                .hasMessageContaining("cavidade 1 duplicada");
        assertThat(salvos).isEmpty();
        assertThat(gravados).isEmpty();
        assertThat(eventos).isEmpty();
    }

    @Test
    void lista_com_cavidade_alem_das_vias_nao_vai_para_a_fila() {
        cabosDaPlanilha = List.of(cabo("W01", "5"));

        assertThatThrownBy(() -> service.solicitar(pedido())).hasMessageContaining("cavidade 5 não existe");
        assertThat(salvos).isEmpty();
    }

    @Test
    void lista_correta_segue_para_a_fila() {
        cabosDaPlanilha = List.of(cabo("W01", "1"), cabo("W02", "2"));

        UUID id = service.solicitar(pedido());

        assertThat(salvos).extracting(DesenhoJob::getId).containsExactly(id);
        assertThat(gravados).hasSize(1);
        assertThat(eventos).hasSize(1);
    }
}
