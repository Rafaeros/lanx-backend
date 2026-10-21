package br.lanx.api.cadastros.materiais;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.MATERIAIS_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.MATERIAIS_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.MATERIAIS_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.MATERIAIS_UPDATE;

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
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.materiais.dtos.CreateMaterialRequest;
import br.lanx.api.cadastros.materiais.dtos.MaterialFilter;
import br.lanx.api.cadastros.materiais.dtos.MaterialResponse;
import br.lanx.api.cadastros.materiais.dtos.UpdateMaterialRequest;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + MATERIAIS_CREATE + "')")
    public ApiResponse<MaterialResponse> create(@Valid @RequestBody CreateMaterialRequest request) {
        return ApiResponse.created("Material cadastrado com sucesso!", materialService.create(request));
    }

    /**
     * Devolve o material com esse código ou cria um novo. 201 se criou, 200 se já
     * existia (nesse caso nada é atualizado). Exige CREATE porque pode criar.
     */
    @PostMapping("/find-or-create")
    @PreAuthorize("hasAuthority('" + MATERIAIS_CREATE + "')")
    public ApiResponse<MaterialResponse> findOrCreate(
            @Valid @RequestBody CreateMaterialRequest request) {
        var result = materialService.findByCodigoOrCreate(request);
        return result.created()
                ? ApiResponse.created("Material cadastrado com sucesso!", result.material())
                : ApiResponse.ok("Material já existia", result.material());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + MATERIAIS_READ + "')")
    public ApiResponse<MaterialResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Material carregado com sucesso!", materialService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + MATERIAIS_READ + "')")
    public ApiResponse<PageResult<MaterialResponse>> search(MaterialFilter filter,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return ApiResponse.ok("Materiais carregados com sucesso!", materialService.findAll(filter, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + MATERIAIS_UPDATE + "')")
    public ApiResponse<MaterialResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateMaterialRequest request) {
        return ApiResponse.ok("Material atualizado com sucesso!", materialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + MATERIAIS_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        materialService.delete(id);
        return ApiResponse.ok("Material excluído com sucesso!", (Void) null);
    }
}
