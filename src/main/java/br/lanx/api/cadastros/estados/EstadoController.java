package br.lanx.api.cadastros.estados;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.estados.dtos.EstadoResponse;
import br.lanx.api.shared.web.ApiResponse;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.ESTADOS_READ;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/estados")
public class EstadoController {

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + ESTADOS_READ + "')")
    public ApiResponse<List<EstadoResponse>> buscar(@RequestParam(required = false) String filtro) {
        return ApiResponse.ok("Estados carregados com sucesso!", estadoService.findAll(filtro));
    }
}
