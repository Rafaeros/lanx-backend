package br.lanx.api.engenharia.operacoes.desenhos.adapter.in.web;

import static br.lanx.api.engenharia.shared.EngenhariaPermissions.DESENHOS_CREATE;
import static br.lanx.api.engenharia.shared.EngenhariaPermissions.DESENHOS_READ;
import static br.lanx.api.engenharia.shared.EngenhariaPermissions.DESENHOS_REPROCESSAR;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.AnaliseListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.ArquivoDesenhoDownload;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoDetalhe;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoResumo;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.AnalisarListaCorteUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.ConsultarDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.ConsultarDesenhoUseCase.ListarDesenhosQuery;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.ReprocessarDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase.SolicitarDesenhoCommand;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase.Upload;
import br.lanx.api.engenharia.operacoes.desenhos.domain.Acabamento;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoOpcoes;
import br.lanx.api.engenharia.operacoes.desenhos.domain.FormatoDesenho;
import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;
import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.security.CurrentUser;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

/** Desenhos de chicote: envia a lista de corte, acompanha o status e baixa o zip gerado pelo worker. */
@RestController
@RequestMapping("/engenharia/operacoes/desenhos")
public class DesenhoController {

    private final SolicitarDesenhoUseCase solicitar;
    private final ConsultarDesenhoUseCase consultar;
    private final ReprocessarDesenhoUseCase reprocessar;
    private final AnalisarListaCorteUseCase analisar;
    private final CurrentUser currentUser;

    DesenhoController(SolicitarDesenhoUseCase solicitar, ConsultarDesenhoUseCase consultar,
            ReprocessarDesenhoUseCase reprocessar, AnalisarListaCorteUseCase analisar, CurrentUser currentUser) {
        this.solicitar = solicitar;
        this.analisar = analisar;
        this.consultar = consultar;
        this.reprocessar = reprocessar;
        this.currentUser = currentUser;
    }

    /** Multipart: "request" (JSON, Content-Type application/json), "listaCorte" (.xlsx) e "desenhoCliente" (.pdf, opcional). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + DESENHOS_CREATE + "')")
    public ApiResponse<CreatedResult> solicitar(
            @Valid @RequestPart("request") SolicitarDesenhoRequest request,
            @RequestPart("listaCorte") MultipartFile listaCorte,
            @RequestPart(value = "desenhoCliente", required = false) MultipartFile desenhoCliente) {
        var cmd = new SolicitarDesenhoCommand(currentUser.id(), request.projeto(), opcoes(request),
                request.projectOverride(), upload(listaCorte), upload(desenhoCliente));
        return ApiResponse.created("Desenho enviado para geração", new CreatedResult(solicitar.solicitar(cmd)));
    }

    /**
     * Confere a lista de corte sem criar job (a tela chama ao escolher o arquivo). Conector no padrão de vias
     * (ex.: CNFAT04CZ = 4 vias) com cavidade inexistente ou duplicada vem como ERRO, e o POST de solicitar
     * recusa a mesma lista com 400.
     */
    @PostMapping(path = "/analise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + DESENHOS_CREATE + "')")
    public ApiResponse<AnaliseListaCorte> analisar(@RequestPart("listaCorte") MultipartFile listaCorte) {
        AnaliseListaCorte analise = analisar.analisar(upload(listaCorte));
        return ApiResponse.ok(analise.temErro() ? "Lista de corte com erros nos conectores"
                : "Lista de corte conferida", analise);
    }

    /** Histórico, mais recentes primeiro. {@code meus=true} mostra só os pedidos de quem está logado. */
    @GetMapping
    @PreAuthorize("hasAuthority('" + DESENHOS_READ + "')")
    public ApiResponse<PageResult<DesenhoResumo>> listar(
            @RequestParam(required = false) String projeto,
            @RequestParam(required = false) StatusDesenho status,
            @RequestParam(defaultValue = "false") boolean meus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var query = new ListarDesenhosQuery(meus ? currentUser.id() : null, projeto, status,
                new PageQuery(page, size));
        return ApiResponse.ok("Desenhos listados", consultar.listar(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + DESENHOS_READ + "')")
    public ApiResponse<DesenhoDetalhe> buscar(@PathVariable UUID id) {
        return ApiResponse.ok("Desenho encontrado", consultar.buscar(id));
    }

    /** Baixa um arquivo do job: a lista de corte, o PDF do cliente ou o zip com os desenhos. */
    @GetMapping("/{id}/arquivos/{arquivoId}")
    @PreAuthorize("hasAuthority('" + DESENHOS_READ + "')")
    public ResponseEntity<InputStreamResource> baixar(@PathVariable UUID id, @PathVariable UUID arquivoId) {
        ArquivoDesenhoDownload arquivo = consultar.baixarArquivo(id, arquivoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(arquivo.nomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(arquivo.contentType()))
                .contentLength(arquivo.tamanhoBytes())
                .body(new InputStreamResource(arquivo.conteudo()));
    }

    /** Gera de novo (mesmo id): só para desenhos concluídos ou com erro. */
    @PostMapping("/{id}/reprocessar")
    @PreAuthorize("hasAuthority('" + DESENHOS_REPROCESSAR + "')")
    public ApiResponse<Void> reprocessar(@PathVariable UUID id) {
        reprocessar.reprocessar(id);
        return ApiResponse.ok("Desenho enviado para gerar de novo", (Void) null);
    }

    // ---- request → tipos da application (o adapter traduz; o domínio não conhece JSON nem multipart) ----

    private static DesenhoOpcoes opcoes(SolicitarDesenhoRequest r) {
        List<String> formatos = r.formatos() == null ? List.of() : r.formatos();
        return new DesenhoOpcoes(
                formatos.stream().map(FormatoDesenho::deFlag).collect(Collectors.toSet()),
                r.acabamento() == null || r.acabamento().isBlank() ? null : Acabamento.valueOf(r.acabamento()),
                r.vistas() == null || r.vistas(),
                r.biblioteca() == null || r.biblioteca(),
                r.dwg() == null || r.dwg());
    }

    private static Upload upload(MultipartFile file) {
        if (file == null || file.isEmpty())
            return null;
        return new Upload(file.getOriginalFilename(), file.getContentType(), file.getSize(), () -> {
            try {
                return file.getInputStream();
            } catch (IOException e) {
                throw new UncheckedIOException("Falha ao ler o arquivo enviado: " + file.getOriginalFilename(), e);
            }
        });
    }
}
