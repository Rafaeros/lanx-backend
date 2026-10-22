package br.lanx.api.cadastros.compradores;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.COMPRADORES_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.COMPRADORES_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.COMPRADORES_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.COMPRADORES_UPDATE;

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

import br.lanx.api.cadastros.compradores.dtos.CompradorRequest;
import br.lanx.api.cadastros.compradores.dtos.CompradorResponse;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/compradores")
public class CompradorController {

    private final CompradorService compradorService;

    public CompradorController(CompradorService compradorService) {
        this.compradorService = compradorService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + COMPRADORES_CREATE + "')")
    public ApiResponse<CompradorResponse> create(@Valid @RequestBody CompradorRequest request) {
        return ApiResponse.created("Comprador cadastrado com sucesso!", compradorService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + COMPRADORES_READ + "')")
    public ApiResponse<PageResult<CompradorResponse>> search(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = "nome") Pageable pageable) {
        return ApiResponse.ok("Compradores listados com sucesso!", compradorService.findAll(busca, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + COMPRADORES_READ + "')")
    public ApiResponse<CompradorResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Comprador encontrado com sucesso!", compradorService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + COMPRADORES_UPDATE + "')")
    public ApiResponse<CompradorResponse> update(@PathVariable UUID id, @Valid @RequestBody CompradorRequest request) {
        return ApiResponse.ok("Comprador atualizado com sucesso!", compradorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + COMPRADORES_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        compradorService.delete(id);
        return ApiResponse.ok("Comprador excluído com sucesso!", (Void) null);
    }
}
