package br.lanx.api.cadastros.unidade_medida.adapter.in.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.unidade_medida.application.port.in.CreateUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidade_medida.application.port.in.DeleteUnidadeMedidaUseCase;
import br.lanx.api.cadastros.unidade_medida.application.port.in.SearchUnidadeMedidaQuery;
import br.lanx.api.cadastros.unidade_medida.application.port.in.SearchUnidadesMedidaUseCase;
import br.lanx.api.cadastros.unidade_medida.application.port.in.UpdateUnidadeMedidaUseCase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/unidade-medida")
@RequiresFeature("cadastros.unidade-medida")
public class UnidadeMedidaController {
    private CreateUnidadeMedidaUseCase createUseCase;
    private SearchUnidadesMedidaUseCase searchUseCase;
    private UpdateUnidadeMedidaUseCase updateUseCase;
    private DeleteUnidadeMedidaUseCase deleteUseCase;

    record CreatedResponse(UUID id) {
    }

    UnidadeMedidaController(CreateUnidadeMedidaUseCase createUseCase, SearchUnidadesMedidaUseCase searchUseCase,
            UpdateUnidadeMedidaUseCase updateUseCase, DeleteUnidadeMedidaUseCase deleteUseCase) {
        this.createUseCase = createUseCase;
        this.searchUseCase = searchUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;

    }

    @PostMapping
    ResponseEntity<ApiResponse<CreatedResponse>> create(@Valid @RequestBody CreateUnidadeMedidaRequest request) {
        CreatedResult result = createUseCase.create(new CreateUnidadeMedidaUseCase.CreateUnidadeMedidaCommand(
                request.nome(), request.sigla(), request.fatorConversao()));
        return ApiResponse.created("Unidade de medida criada!", new CreatedResponse(result.id())).toEntity();
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResult<SearchUnidadeMedidaResponse>>> search(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        PageQuery pageQuery = new PageQuery(page, size);
        SearchUnidadeMedidaQuery query = new SearchUnidadeMedidaQuery(busca, pageQuery);
        var result = searchUseCase.search(query);
        var responseResult = result.map(SearchUnidadeMedidaResponse::from);
        return ApiResponse.ok("Unidades de medidas encontradas", responseResult).toEntity();
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateUnidadeMedidaRequest request) {
        updateUseCase.update(new UpdateUnidadeMedidaUseCase.UpdateUnidadeMedidaCommand(
                id,
                request.nome(),
                request.sigla(),
                request.fatorConversao()));
        return ApiResponse.ok("Unidade de medida atualizada com suceso!", (Void) null).toEntity();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteUseCase.delete(id);
        return ApiResponse.ok("Unidade de medida excluida com suceso!", (Void) null).toEntity();
    }

}
