package br.lanx.api.cadastros.tiponaoconformidade;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.TIPOS_NAO_CONFORMIDADE_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.TIPOS_NAO_CONFORMIDADE_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.TIPOS_NAO_CONFORMIDADE_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.TIPOS_NAO_CONFORMIDADE_UPDATE;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.tiponaoconformidade.dtos.CreateTipoNaoConformidadeRequest;
import br.lanx.api.cadastros.tiponaoconformidade.dtos.TipoNaoConformidadeResponse;
import br.lanx.api.cadastros.tiponaoconformidade.dtos.UpdateTipoNaoConformidadeRequest;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/tipos-nao-conformidade")
public class TipoNaoConformidadeController {

    private final TipoNaoConformidadeService tipoNaoConformidadeService;

    public TipoNaoConformidadeController(TipoNaoConformidadeService tipoNaoConformidadeService) {
        this.tipoNaoConformidadeService = tipoNaoConformidadeService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + TIPOS_NAO_CONFORMIDADE_CREATE + "')")
    public ApiResponse<TipoNaoConformidadeResponse> create(
            @Valid @RequestBody CreateTipoNaoConformidadeRequest request) {
        return ApiResponse.created("Tipo de não conformidade cadastrado com sucesso!",
                tipoNaoConformidadeService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + TIPOS_NAO_CONFORMIDADE_READ + "')")
    public ApiResponse<PageResult<TipoNaoConformidadeResponse>> getAll(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = "nome") Pageable pageable) {
        return ApiResponse.ok("Tipos de não conformidade listados com sucesso!",
                tipoNaoConformidadeService.findAll(busca, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TIPOS_NAO_CONFORMIDADE_READ + "')")
    public ApiResponse<TipoNaoConformidadeResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok("Tipo de não conformidade encontrado com sucesso",
                tipoNaoConformidadeService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TIPOS_NAO_CONFORMIDADE_UPDATE + "')")
    public ApiResponse<TipoNaoConformidadeResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateTipoNaoConformidadeRequest request) {
        return ApiResponse.ok("Tipo de não conformidade atualizado com sucesso!",
                tipoNaoConformidadeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + TIPOS_NAO_CONFORMIDADE_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        tipoNaoConformidadeService.delete(id);
        return ApiResponse.ok("Tipo de não conformidade excluído com sucesso", (Void) null);
    }
}
