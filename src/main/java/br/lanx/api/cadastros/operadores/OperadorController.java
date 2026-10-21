package br.lanx.api.cadastros.operadores;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.OPERADORES_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.OPERADORES_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.OPERADORES_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.OPERADORES_UPDATE;

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

import br.lanx.api.cadastros.operadores.dtos.CreateOperadorRequest;
import br.lanx.api.cadastros.operadores.dtos.OperadorResponse;
import br.lanx.api.cadastros.operadores.dtos.UpdateOperadorRequest;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/operadores")
public class OperadorController {

    private final OperadorService operadorService;

    public OperadorController(OperadorService operadorService) {
        this.operadorService = operadorService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + OPERADORES_CREATE + "')")
    public ApiResponse<OperadorResponse> create(@Valid @RequestBody CreateOperadorRequest request) {
        return ApiResponse.created("Operador cadastrado com sucesso", operadorService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + OPERADORES_READ + "')")
    public ApiResponse<PageResult<OperadorResponse>> getAll(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = "nome") Pageable pageable) {
        return ApiResponse.ok("Operadores listados com sucesso", operadorService.findAll(busca, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + OPERADORES_READ + "')")
    public ApiResponse<OperadorResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok("Operador encontrado com sucesso!", operadorService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + OPERADORES_UPDATE + "')")
    public ApiResponse<OperadorResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateOperadorRequest request) {
        return ApiResponse.ok("Operador atualizado com sucesso!", operadorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + OPERADORES_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        operadorService.delete(id);
        return ApiResponse.ok("Operador deletado com sucesso!", (Void) null);
    }
}
