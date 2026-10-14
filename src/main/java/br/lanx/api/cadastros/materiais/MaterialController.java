package br.lanx.api.cadastros.materiais;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.materiais.dtos.CreateMaterialRequest;
import br.lanx.api.cadastros.materiais.dtos.MaterialFilter;
import br.lanx.api.cadastros.materiais.dtos.MaterialResponse;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;

@RestController
@RequestMapping("/materiais")
@RequiresFeature("cadastros.material")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PostMapping 
    public ApiResponse<MaterialResponse> create(CreateMaterialRequest request) {
        return ApiResponse.created("Material cadastrado com sucesso!", materialService.create(request));
    }

    @GetMapping 
    public ApiResponse<PageResult<MaterialResponse>> search(MaterialFilter filter,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return ApiResponse.ok("Materiais carregados com sucesso!", materialService.findAll(filter, pageable));
    }

}
