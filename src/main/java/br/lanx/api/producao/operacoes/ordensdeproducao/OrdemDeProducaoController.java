package br.lanx.api.producao.operacoes.ordensdeproducao;

import static br.lanx.api.producao.shared.ProducaoPermissions.ORDENS_PRODUCAO_CREATE;
import static br.lanx.api.producao.shared.ProducaoPermissions.ORDENS_PRODUCAO_DELETE;
import static br.lanx.api.producao.shared.ProducaoPermissions.ORDENS_PRODUCAO_IMPORTAR;
import static br.lanx.api.producao.shared.ProducaoPermissions.ORDENS_PRODUCAO_READ;
import static br.lanx.api.producao.shared.ProducaoPermissions.ORDENS_PRODUCAO_UPDATE;

import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.ImportarOrdensResponse;
import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.OrdemDeProducaoRequest;
import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.OrdemDeProducaoResponse;
import br.lanx.api.producao.operacoes.ordensdeproducao.importacao.OrdemDeProducaoImportacaoService;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/producao/operacoes/ordens-de-producao")
public class OrdemDeProducaoController {

    private final OrdemDeProducaoService service;
    private final OrdemDeProducaoImportacaoService importacaoService;

    public OrdemDeProducaoController(OrdemDeProducaoService service,
            OrdemDeProducaoImportacaoService importacaoService) {
        this.service = service;
        this.importacaoService = importacaoService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + ORDENS_PRODUCAO_CREATE + "')")
    public ApiResponse<OrdemDeProducaoResponse> create(@Valid @RequestBody OrdemDeProducaoRequest request) {
        return ApiResponse.created("Ordem de produção cadastrada com sucesso!", service.create(request));
    }

    /**
     * Planilha de OPs exportada do ERP (.xlsx): atualiza as existentes pelo código e cria as novas,
     * cadastrando cliente e produto que faltarem. Finalizadas só entram com {@code incluirFinalizadas=true}.
     */
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + ORDENS_PRODUCAO_IMPORTAR + "')")
    public ApiResponse<ImportarOrdensResponse> importar(@RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam(defaultValue = "false") boolean incluirFinalizadas) throws IOException {
        return ApiResponse.ok("Planilha de ordens de produção importada",
                importacaoService.importar(arquivo.getInputStream(), incluirFinalizadas));
    }

    /** Mais urgentes primeiro (previsão mais próxima). */
    @GetMapping
    @PreAuthorize("hasAuthority('" + ORDENS_PRODUCAO_READ + "')")
    public ApiResponse<PageResult<OrdemDeProducaoResponse>> search(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = { "previsao", "codigo" }, direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ApiResponse.ok("Ordens de produção listadas com sucesso!", service.findAll(busca, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + ORDENS_PRODUCAO_READ + "')")
    public ApiResponse<OrdemDeProducaoResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Ordem de produção encontrada com sucesso!", service.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + ORDENS_PRODUCAO_UPDATE + "')")
    public ApiResponse<OrdemDeProducaoResponse> update(@PathVariable UUID id,
            @Valid @RequestBody OrdemDeProducaoRequest request) {
        return ApiResponse.ok("Ordem de produção atualizada com sucesso!", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + ORDENS_PRODUCAO_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("Ordem de produção excluída com sucesso!", (Void) null);
    }
}
