package br.lanx.api.cadastros.fornecedores;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.FORNECEDOR_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.FORNECEDOR_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.FORNECEDOR_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.FORNECEDOR_UPDATE;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import br.lanx.api.cadastros.fornecedores.dtos.CreateFornecedorRequest;
import br.lanx.api.cadastros.fornecedores.dtos.FornecedorResponse;
import br.lanx.api.cadastros.fornecedores.dtos.UpdateFornecedorRequest;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + FORNECEDOR_CREATE + "')")
    public ResponseEntity<ApiResponse<FornecedorResponse>> create(@Valid @RequestBody CreateFornecedorRequest request) {
        return ApiResponse.created("Fornecedor cadastrado com sucesso!", fornecedorService.create(request)).toEntity();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + FORNECEDOR_READ + "')")
    public ApiResponse<FornecedorResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Fornecedor carregado com sucesso!", fornecedorService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + FORNECEDOR_READ + "')")
    public ApiResponse<PageResult<FornecedorResponse>> search(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = "nome") Pageable pageable) {
        return ApiResponse.ok("Fornecedores carregados com sucesso!", fornecedorService.findAll(busca, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + FORNECEDOR_UPDATE + "')")
    public ApiResponse<FornecedorResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateFornecedorRequest request) {
        return ApiResponse.ok("Fornecedor atualizado com sucesso!", fornecedorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + FORNECEDOR_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        fornecedorService.delete(id);
        return ApiResponse.ok("Fornecedor excluído com sucesso!", (Void) null);
    }
}
