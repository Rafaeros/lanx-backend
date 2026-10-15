package br.lanx.api.cadastros.unidademedida.adapter.in.web;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.UNIDADE_MEDIDA_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.UNIDADE_MEDIDA_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.UNIDADE_MEDIDA_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.UNIDADE_MEDIDA_UPDATE;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
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

import br.lanx.api.cadastros.unidademedida.application.port.in.CreateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.in.DeleteUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.in.FindOrCreateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.in.FindUnidadeMedidaByIdUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.in.SearchUnidadeMedidaQuery;
import br.lanx.api.cadastros.unidademedida.application.port.in.SearchUnidadesMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.application.port.in.UpdateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidademedida.domain.exceptions.UnidadeMedidaNotFoundException;
import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/unidade-medida")
public class UnidadeMedidaController {
    private final CreateUnidadeMedidaUseCase createUseCase;
    private final FindUnidadeMedidaByIdUseCase findByIdUseCase;
    private final SearchUnidadesMedidaUseCase searchUseCase;
    private final UpdateUnidadeMedidaUseCase updateUseCase;
    private final DeleteUnidadeMedidaUseCase deleteUseCase;
    private final FindOrCreateUnidadeMedidaUseCase findOrCreateUseCase;

    record CreatedResponse(UUID id) {
    }

    UnidadeMedidaController(CreateUnidadeMedidaUseCase createUseCase, FindUnidadeMedidaByIdUseCase findByIdUseCase,
            SearchUnidadesMedidaUseCase searchUseCase, UpdateUnidadeMedidaUseCase updateUseCase,
            DeleteUnidadeMedidaUseCase deleteUseCase, FindOrCreateUnidadeMedidaUseCase findOrCreateUseCase) {
        this.createUseCase = createUseCase;
        this.findByIdUseCase = findByIdUseCase;
        this.searchUseCase = searchUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.findOrCreateUseCase = findOrCreateUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + UNIDADE_MEDIDA_CREATE + "')")
    ResponseEntity<ApiResponse<CreatedResponse>> create(@Valid @RequestBody CreateUnidadeMedidaRequest request) {
        CreatedResult result = createUseCase.create(new CreateUnidadeMedidaUseCase.CreateUnidadeMedidaCommand(
                request.nome(), request.sigla()));
        return ApiResponse.created("Unidade de medida criada!", new CreatedResponse(result.id())).toEntity();
    }

    /**
     * Devolve a unidade com esse nome ou cria uma nova com a sigla informada.
     * 201 se criou, 200 se já existia. Exige CREATE porque pode criar.
     */
    @PostMapping("/find-or-create")
    @PreAuthorize("hasAuthority('" + UNIDADE_MEDIDA_CREATE + "')")
    ResponseEntity<ApiResponse<SearchUnidadeMedidaResponse>> findOrCreate(
            @Valid @RequestBody CreateUnidadeMedidaRequest request) {
        var result = findOrCreateUseCase.findByNomeOrCreate(
                new FindOrCreateUnidadeMedidaUseCase.Command(request.nome(), request.sigla()));
        var body = SearchUnidadeMedidaResponse.from(result.unidade());
        return result.created()
                ? ApiResponse.created("Unidade de medida criada!", body).toEntity()
                : ApiResponse.ok("Unidade de medida já existia", body).toEntity();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + UNIDADE_MEDIDA_READ + "')")
    public ApiResponse<SearchUnidadeMedidaResponse> get(@PathVariable UUID id) {
        var unidade = findByIdUseCase.findById(id).orElseThrow(() -> new UnidadeMedidaNotFoundException(id));
        return ApiResponse.ok("Unidade de medida encontrada", SearchUnidadeMedidaResponse.from(unidade));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + UNIDADE_MEDIDA_READ + "')")
    public ApiResponse<PageResult<SearchUnidadeMedidaResponse>> search(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        PageQuery pageQuery = new PageQuery(page, size);
        SearchUnidadeMedidaQuery query = new SearchUnidadeMedidaQuery(busca, pageQuery);
        var result = searchUseCase.search(query);
        var responseResult = result.map(SearchUnidadeMedidaResponse::from);
        return ApiResponse.ok("Unidades de medidas encontradas", responseResult);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + UNIDADE_MEDIDA_UPDATE + "')")
    public ApiResponse<Void> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateUnidadeMedidaRequest request) {
        updateUseCase.update(new UpdateUnidadeMedidaUseCase.UpdateUnidadeMedidaCommand(
                id,
                request.nome(),
                request.sigla()));
        return ApiResponse.ok("Unidade de medida atualizada com sucesso!", (Void) null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + UNIDADE_MEDIDA_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        deleteUseCase.delete(id);
        return ApiResponse.ok("Unidade de medida excluída com sucesso!", (Void) null);
    }
}
