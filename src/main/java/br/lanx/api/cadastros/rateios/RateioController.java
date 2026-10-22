package br.lanx.api.cadastros.rateios;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.RATEIOS_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.RATEIOS_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.RATEIOS_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.RATEIOS_UPDATE;

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

import br.lanx.api.cadastros.rateios.dtos.RateioRequest;
import br.lanx.api.cadastros.rateios.dtos.RateioResponse;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rateios")
public class RateioController {

    private final RateioService rateioService;

    public RateioController(RateioService rateioService) {
        this.rateioService = rateioService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + RATEIOS_CREATE + "')")
    public ApiResponse<RateioResponse> create(@Valid @RequestBody RateioRequest request) {
        return ApiResponse.created("Rateio cadastrado com sucesso!", rateioService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + RATEIOS_READ + "')")
    public ApiResponse<PageResult<RateioResponse>> search(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = "nome") Pageable pageable) {
        return ApiResponse.ok("Rateios listados com sucesso!", rateioService.findAll(busca, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RATEIOS_READ + "')")
    public ApiResponse<RateioResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Rateio encontrado com sucesso!", rateioService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RATEIOS_UPDATE + "')")
    public ApiResponse<RateioResponse> update(@PathVariable UUID id, @Valid @RequestBody RateioRequest request) {
        return ApiResponse.ok("Rateio atualizado com sucesso!", rateioService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RATEIOS_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        rateioService.delete(id);
        return ApiResponse.ok("Rateio excluído com sucesso!", (Void) null);
    }
}
