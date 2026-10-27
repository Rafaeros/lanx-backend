package br.lanx.api.cadastros.clientes;

import static br.lanx.api.cadastros.shared.CadastrosPermissions.CLIENTES_CREATE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.CLIENTES_DELETE;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.CLIENTES_READ;
import static br.lanx.api.cadastros.shared.CadastrosPermissions.CLIENTES_UPDATE;

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

import br.lanx.api.cadastros.clientes.dtos.ClienteRequest;
import br.lanx.api.cadastros.clientes.dtos.ClienteResponse;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + CLIENTES_CREATE + "')")
    public ApiResponse<ClienteResponse> create(@Valid @RequestBody ClienteRequest request) {
        return ApiResponse.created("Cliente cadastrado com sucesso!", clienteService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + CLIENTES_READ + "')")
    public ApiResponse<PageResult<ClienteResponse>> search(@RequestParam(required = false) String busca,
            @PageableDefault(page = 0, size = 20, sort = "nome") Pageable pageable) {
        return ApiResponse.ok("Clientes listados com sucesso!", clienteService.findAll(busca, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + CLIENTES_READ + "')")
    public ApiResponse<ClienteResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Cliente encontrado com sucesso!", clienteService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + CLIENTES_UPDATE + "')")
    public ApiResponse<ClienteResponse> update(@PathVariable UUID id, @Valid @RequestBody ClienteRequest request) {
        return ApiResponse.ok("Cliente atualizado com sucesso!", clienteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + CLIENTES_DELETE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        clienteService.delete(id);
        return ApiResponse.ok("Cliente excluído com sucesso!", (Void) null);
    }
}
