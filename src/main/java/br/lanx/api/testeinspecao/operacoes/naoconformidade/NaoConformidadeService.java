package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import br.lanx.api.cadastros.departamentos.DepartamentoService;
import br.lanx.api.cadastros.operadores.OperadorService;
import br.lanx.api.cadastros.tiponaoconformidade.TipoNaoConformidadeService;
import br.lanx.api.producao.operacoes.ordensdeproducao.OrdemDeProducaoService;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.application.port.out.AnexoStoragePort;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.AnexoDownload;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.AnexoResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.CreateNaoConformidadeRequest;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeFiltro;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeIndicadoresResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.UpdateNaoConformidadeRequest;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.events.NaoConformidadeCriadaEvent;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.exceptions.AnexoInvalidoException;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.exceptions.NaoConformidadeNotFoundException;
import br.lanx.api.shared.domain.Ids;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class NaoConformidadeService {

    private static final Logger log = LoggerFactory.getLogger(NaoConformidadeService.class);

    private static final int MAX_ANEXOS = 10;
    private static final int MAX_NOME_ARQUIVO = 255;
    private static final String CONTENT_TYPE_PADRAO = "application/octet-stream";
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf");

    private final NaoConformidadeRepository naoConformidadeRepository;
    private final NaoConformidadeAnexoRepository naoConformidadeAnexoRepository;
    private final TipoNaoConformidadeService tipoNaoConformidadeService;
    private final DepartamentoService departamentoService;
    private final OrdemDeProducaoService ordemDeProducaoService;
    private final OperadorService operadorService;
    private final AnexoStoragePort storage;
    private final ApplicationEventPublisher events;

    public NaoConformidadeService(NaoConformidadeRepository naoConformidadeRepository,
            NaoConformidadeAnexoRepository naoConformidadeAnexoRepository,
            TipoNaoConformidadeService tipoNaoConformidadeService, DepartamentoService departamentoService,
            OrdemDeProducaoService ordemDeProducaoService, OperadorService operadorService, AnexoStoragePort storage,
            ApplicationEventPublisher events) {
        this.naoConformidadeRepository = naoConformidadeRepository;
        this.naoConformidadeAnexoRepository = naoConformidadeAnexoRepository;
        this.tipoNaoConformidadeService = tipoNaoConformidadeService;
        this.departamentoService = departamentoService;
        this.ordemDeProducaoService = ordemDeProducaoService;
        this.operadorService = operadorService;
        this.storage = storage;
        this.events = events;
    }

    @Transactional
    public NaoConformidadeResponse create(CreateNaoConformidadeRequest request, List<MultipartFile> files) {
        List<MultipartFile> anexos = files == null ? List.of() : files;
        validarAnexos(anexos);

        NaoConformidade nc = new NaoConformidade();
        preencher(nc, request.tipoNaoConformidadeId(), request.departamentoId(), request.ordemDeProducaoId(),
                request.operadorId(), request.quantidadeNaoConforme(), request.dataNaoConformidade());
        nc = naoConformidadeRepository.save(nc);

        List<String> chavesSalvas = removerArquivosSeDerRollback();
        List<AnexoResponse> anexosSalvos = new ArrayList<>();
        for (MultipartFile file : anexos) {
            String chave = "nao-conformidades/" + nc.getId() + "/" + Ids.newId();
            storage.salvar(chave, abrir(file));
            chavesSalvas.add(chave);

            NaoConformidadeAnexo anexo = new NaoConformidadeAnexo();
            anexo.setNaoConformidade(nc);
            anexo.setChaveArmazenamento(chave);
            anexo.setNomeArquivo(nomeArquivo(file));
            anexo.setContentType(contentType(file));
            anexo.setTamanhoBytes(file.getSize());
            anexosSalvos.add(AnexoResponse.fromEntity(naoConformidadeAnexoRepository.save(anexo)));
        }
        events.publishEvent(new NaoConformidadeCriadaEvent(nc.getId(), nc.getDepartamento().getId(),
                nc.getTipo().getNome(), nc.getDataNaoConformidade(), Instant.now()));
        return NaoConformidadeResponse.fromEntity(nc, anexosSalvos);
    }

    @Transactional(readOnly = true)
    public PageResult<NaoConformidadeResponse> findAll(NaoConformidadeFiltro filtro, Pageable pageable) {
        return PageResult.from(naoConformidadeRepository
                .buscar(termoBusca(filtro.busca()), filtro.inicio(), filtro.fim(), pageable)
                .map(NaoConformidadeResponse::fromEntity));
    }

    /** Todas as NCs do filtro, sem paginação, na ordem da planilha (data e OP). */
    @Transactional(readOnly = true)
    public List<NaoConformidadeResponse> listarParaExportar(NaoConformidadeFiltro filtro) {
        return naoConformidadeRepository.listar(termoBusca(filtro.busca()), filtro.inicio(), filtro.fim())
                .stream().map(NaoConformidadeResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public NaoConformidadeResponse findById(UUID id) {
        NaoConformidade nc = naoConformidadeRepository.buscarDetalhe(id).orElseThrow(() -> notFound(id));
        List<AnexoResponse> anexos = naoConformidadeAnexoRepository.findAllByNaoConformidade_IdOrderByCreatedAt(id)
                .stream().map(AnexoResponse::fromEntity).toList();
        return NaoConformidadeResponse.fromEntity(nc, anexos);
    }

    /** Indicadores do período (e da busca, se houver) para os cards e gráficos. */
    @Transactional(readOnly = true)
    public NaoConformidadeIndicadoresResponse indicadores(NaoConformidadeFiltro filtro) {
        return NaoConformidadeIndicadores.calcular(listarParaExportar(filtro), filtro.dataInicio(), filtro.dataFim());
    }

    @Transactional
    public NaoConformidadeResponse update(UUID id, UpdateNaoConformidadeRequest request) {
        NaoConformidade nc = naoConformidadeRepository.findById(id).orElseThrow(() -> notFound(id));
        preencher(nc, request.tipoNaoConformidadeId(), request.departamentoId(), request.ordemDeProducaoId(),
                request.operadorId(), request.quantidadeNaoConforme(), request.dataNaoConformidade());
        return NaoConformidadeResponse.fromEntity(naoConformidadeRepository.save(nc));
    }

    /**
     * Exclusão lógica (@SQLDelete): os anexos continuam guardados junto do
     * registro.
     */
    @Transactional
    public void delete(UUID id) {
        if (!naoConformidadeRepository.existsById(id)) {
            throw notFound(id);
        }
        naoConformidadeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AnexoDownload downloadAnexo(UUID naoConformidadeId, UUID anexoId) {
        // Garante que a NC existe e não foi excluída: o filtro por FK abaixo não aplica
        // o @SQLRestriction
        if (!naoConformidadeRepository.existsById(naoConformidadeId)) {
            throw notFound(naoConformidadeId);
        }
        NaoConformidadeAnexo anexo = naoConformidadeAnexoRepository
                .findByIdAndNaoConformidade_Id(anexoId, naoConformidadeId)
                .orElseThrow(() -> new NaoConformidadeNotFoundException("Anexo não encontrado: " + anexoId));
        return new AnexoDownload(anexo.getNomeArquivo(), anexo.getContentType(), anexo.getTamanhoBytes(),
                storage.carregar(anexo.getChaveArmazenamento()));
    }

    private void preencher(NaoConformidade nc, UUID tipoId, UUID departamentoId, UUID ordemDeProducaoId,
            UUID operadorId, BigDecimal quantidade, LocalDate data) {
        nc.setTipo(tipoNaoConformidadeService.findByIdOrThrow(tipoId));
        nc.setDepartamento(departamentoService.findByIdOrThrow(departamentoId));
        nc.setOrdemDeProducao(ordemDeProducaoService.findByIdOrThrow(ordemDeProducaoId));
        nc.setOperador(operadorId != null ? operadorService.findByIdOrThrow(operadorId) : null);
        nc.setQuantidadeNaoConforme(quantidade);
        nc.setDataNaoConformidade(data);
    }

    private static String termoBusca(String busca) {
        return busca == null || busca.isBlank() ? "" : "%" + busca.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private void validarAnexos(List<MultipartFile> files) {
        if (files.size() > MAX_ANEXOS) {
            throw new AnexoInvalidoException("Envie no máximo " + MAX_ANEXOS + " anexos.");
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new AnexoInvalidoException("O arquivo '" + nomeArquivo(file) + "' está vazio.");
            }
            if (!TIPOS_PERMITIDOS.contains(contentType(file))) {
                throw new AnexoInvalidoException("Tipo de arquivo não permitido: " + nomeArquivo(file)
                        + ". Aceitos: JPEG, PNG, WEBP e PDF.");
            }
        }
    }

    /**
     * O INSERT só roda no commit, depois que este método já retornou: um FK ou
     * CHECK que falhe ali não passa por try/catch daqui. Por isso a limpeza dos
     * arquivos fica amarrada ao fim da transação, e não a um catch.
     */
    private List<String> removerArquivosSeDerRollback() {
        List<String> chaves = new ArrayList<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    chaves.forEach(NaoConformidadeService.this::removerSemFalhar);
                }
            }
        });
        return chaves;
    }

    private void removerSemFalhar(String chave) {
        try {
            storage.remover(chave);
        } catch (RuntimeException e) {
            log.warn("Anexo órfão não removido após rollback: {}", chave, e);
        }
    }

    private static InputStream abrir(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Descarta caminho que alguns navegadores mandam junto (ex.:
     * C:\fakepath\foto.jpg).
     */
    private static String nomeArquivo(MultipartFile file) {
        String original = file.getOriginalFilename();
        String nome = original == null ? null : StringUtils.getFilename(original.replace('\\', '/'));
        if (nome == null || nome.isBlank()) {
            nome = "anexo";
        }
        return nome.length() > MAX_NOME_ARQUIVO ? nome.substring(0, MAX_NOME_ARQUIVO) : nome;
    }

    private static String contentType(MultipartFile file) {
        String tipo = file.getContentType();
        return tipo == null || tipo.isBlank() ? CONTENT_TYPE_PADRAO : tipo.toLowerCase(Locale.ROOT);
    }

    private static NaoConformidadeNotFoundException notFound(UUID id) {
        return new NaoConformidadeNotFoundException("Não conformidade não encontrada: " + id);
    }
}
