package br.lanx.api.rh.cadastros.colaboradores;

import static br.lanx.api.rh.shared.RecursosHumanosPermissions.COLABORADORES_CREATE;
import static br.lanx.api.rh.shared.RecursosHumanosPermissions.COLABORADORES_READ;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.rh.cadastros.colaboradores.dtos.ColaboradorDetailResponse;
import br.lanx.api.rh.cadastros.colaboradores.dtos.ColaboradorResponse;
import br.lanx.api.rh.cadastros.colaboradores.dtos.CreateColaboradorRequest;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/recursos-humanos/cadastros/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + COLABORADORES_CREATE + "')")
    public ApiResponse<ColaboradorResponse> create(@Valid @RequestBody CreateColaboradorRequest req) {
        return ApiResponse.created("Colaborador cadastrado com sucesso!", colaboradorService.create(req));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + COLABORADORES_READ + "')")
    public ApiResponse<PageResult<ColaboradorResponse>> getAll(
        @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return ApiResponse.ok("Colaboradores encontrados com sucesso", colaboradorService.listAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + COLABORADORES_READ + "')")
    public ApiResponse<ColaboradorDetailResponse> getDetailsById(@PathVariable UUID id) {
        return ApiResponse.ok("Colaborador encontrado com suceso", colaboradorService.findDetailById(id));
    }

    // TODO: corrigir a mensagem do getDetailsById: "suceso" -> "sucesso".
    // TODO: PUT /{id} -> atualizar dados básicos (nome, departamento) com COLABORADORES_UPDATE.
    // TODO: DELETE /{id} -> soft delete (@SQLDelete já existe na entidade) com COLABORADORES_DELETE.
    // TODO: sub-recursos de endereço, com COLABORADORES_UPDATE:
    //       POST /{id}/enderecos, PUT /{id}/enderecos/{enderecoId}, DELETE /{id}/enderecos/{enderecoId}.
    // TODO: o mesmo para telefones e emails: POST/PUT/DELETE em /{id}/telefones/{telefoneId} e /{id}/emails/{emailId}.
    // TODO: filtros no getAll (ex.: nome, departamentoId) via um record ColaboradorFilter, como em MaterialFilter.
    // TODO: COLABORADORES_IMPORTAR / COLABORADORES_EXPORTAR estão declaradas em RecursosHumanosPermissions
    //       sem endpoint: implementar ou remover.

}
