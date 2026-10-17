package br.lanx.api.cadastros.pais;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.PAISES_READ;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.pais.dtos.PaisResponse;
import br.lanx.api.shared.web.ApiResponse;

/** Países vêm do seeder (ISO 3166): só leitura, para selects como o do fornecedor. */
@RestController
@RequestMapping("/paises")
public class PaisController {

    private final PaisLookUpServiice paisLookUpService;

    public PaisController(PaisLookUpServiice paisLookUpService) {
        this.paisLookUpService = paisLookUpService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PAISES_READ + "')")
    public ApiResponse<List<PaisResponse>> list() {
        return ApiResponse.ok("Países carregados com sucesso!", paisLookUpService.findAll());
    }
}
