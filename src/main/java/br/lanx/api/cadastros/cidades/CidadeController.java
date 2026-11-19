package br.lanx.api.cadastros.cidades;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.CIDADES_READ;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.cidades.dtos.CidadeResponse;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;


@RestController
@RequestMapping("/cidades")
public class CidadeController {
    
    private final CidadeService cidadeService;

    public CidadeController(CidadeService cidadeService) {
        this.cidadeService = cidadeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + CIDADES_READ + "')")
    public ApiResponse<PageResult<CidadeResponse>> buscar(@RequestParam(required=false) String nome, @PageableDefault(page = 0, size = 20) Pageable pageable){
        return ApiResponse.ok("Cidades listadas com sucesso!", cidadeService.buscar(nome, pageable));
    }    

}
