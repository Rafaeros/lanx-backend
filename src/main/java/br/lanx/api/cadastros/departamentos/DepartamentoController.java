package br.lanx.api.cadastros.departamentos;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.departamentos.dtos.CreateDepartamentoRequest;
import br.lanx.api.cadastros.departamentos.dtos.DepartamentoResponse;
import br.lanx.api.cadastros.departamentos.dtos.UpdateDepartamentoRequest;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.DEPARTAMENTO_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.DEPARTAMENTO_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.DEPARTAMENTO_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.DEPARTAMENTO_UPDATE;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + DEPARTAMENTO_CREATE + "')")
    public ApiResponse<DepartamentoResponse> create(@Valid @RequestBody CreateDepartamentoRequest request) {
        return ApiResponse.created("Departamento cadastrado com sucesso!", departamentoService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + DEPARTAMENTO_READ + "')")
    public ApiResponse<PageResult<DepartamentoResponse>> getAll(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = "nome") Pageable pageable) {
        return ApiResponse.ok("Departamentos listados com sucesso!", departamentoService.findAll(busca, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + DEPARTAMENTO_READ + "')")
    public ApiResponse<DepartamentoResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok("Departamento encontrado com sucesso", departamentoService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + DEPARTAMENTO_UPDATE + "')")
    public ApiResponse<DepartamentoResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateDepartamentoRequest request) {
        return ApiResponse.ok("Departamento atualizado com sucesso!", departamentoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + DEPARTAMENTO_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        departamentoService.delete(id);
        return ApiResponse.ok("Departamento deletado com sucesso", (Void) null);
    }

}
