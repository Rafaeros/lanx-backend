package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.AnexoDownload;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.CreateNaoConformidadeRequest;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeFiltro;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeIndicadoresResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.UpdateNaoConformidadeRequest;
import br.lanx.api.testeinspecao.shared.TesteInspecaoPermissions;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import br.lanx.api.shared.web.planilha.Planilha;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/teste-inspecao/operacoes/nao-conformidade")
public class NaoConformidadeController {

    private final NaoConformidadeService naoConformidadeService;

    public NaoConformidadeController(NaoConformidadeService naoConformidadeService) {
        this.naoConformidadeService = naoConformidadeService;
    }

    /** A parte "request" precisa vir com Content-Type application/json. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_CREATE + "')")
    public ApiResponse<NaoConformidadeResponse> create(
            @Valid @RequestPart(name = "request") CreateNaoConformidadeRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.created("Não conformidade cadastrada com sucesso!",
                naoConformidadeService.create(request, files));
    }

    /** Mais recentes primeiro. Sem anexos: use o detalhe. */
    @GetMapping
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_READ + "')")
    public ApiResponse<PageResult<NaoConformidadeResponse>> search(NaoConformidadeFiltro filtro,
            @PageableDefault(page = 0, size = 20, sort = { "dataNaoConformidade",
                    "createdAt" }, direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok("Não conformidades listadas com sucesso!",
                naoConformidadeService.findAll(filtro, pageable));
    }

    /** Totais e quantidades por departamento, tipo, cliente e mês, para os cards e gráficos. */
    @GetMapping("/indicadores")
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_READ + "')")
    public ApiResponse<NaoConformidadeIndicadoresResponse> indicadores(NaoConformidadeFiltro filtro) {
        return ApiResponse.ok("Indicadores de não conformidade calculados", naoConformidadeService.indicadores(filtro));
    }

    /** Planilha (.xlsx) com as NCs do filtro, uma por linha, com os dados da OP. */
    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_EXPORTAR + "')")
    public ResponseEntity<byte[]> exportar(NaoConformidadeFiltro filtro) {
        var itens = naoConformidadeService.listarParaExportar(filtro);
        return Planilha.download("nao-conformidades_" + LocalDate.now() + ".xlsx",
                NaoConformidadePlanilha.gerar(itens,
                        NaoConformidadeIndicadores.calcular(itens, filtro.dataInicio(), filtro.dataFim()), filtro));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_READ + "')")
    public ApiResponse<NaoConformidadeResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Não conformidade encontrada com sucesso!", naoConformidadeService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_UPDATE + "')")
    public ApiResponse<NaoConformidadeResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateNaoConformidadeRequest request) {
        return ApiResponse.ok("Não conformidade atualizada com sucesso!", naoConformidadeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        naoConformidadeService.delete(id);
        return ApiResponse.ok("Não conformidade excluída com sucesso!", (Void) null);
    }

    @GetMapping("/{id}/anexos/{anexoId}")
    @PreAuthorize("hasAuthority('" + TesteInspecaoPermissions.NAOCONFORMIDADES_READ + "')")
    public ResponseEntity<InputStreamResource> downloadAnexo(@PathVariable UUID id, @PathVariable UUID anexoId) {
        AnexoDownload anexo = naoConformidadeService.downloadAnexo(id, anexoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(anexo.nomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.parseMediaType(anexo.contentType()))
                .contentLength(anexo.tamanhoBytes())
                .body(new InputStreamResource(anexo.conteudo()));
    }
}
