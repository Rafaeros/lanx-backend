package br.lanx.api.cadastros.unidade_medida.adapter.in.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.cadastros.unidade_medida.application.port.in.CreateUnidadeMedidaUseCase;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.lanx.api.shared.application.CreatedResult;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.web.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/unidade-medida")
@RequiresFeature("cadastros.unidade-medida")
public class UnidadeMedidaController {
    private CreateUnidadeMedidaUseCase createUseCase;

    record CreatedResponse(UUID id) {
    }

    UnidadeMedidaController(CreateUnidadeMedidaUseCase createUseCase) {
        this.createUseCase = createUseCase;
    }

    @PostMapping
    ResponseEntity<ApiResponse<CreatedResponse>> create(@Valid @RequestBody CreateUnidadeMedidaRequest request) {
        CreatedResult result = createUseCase.create(new CreateUnidadeMedidaUseCase.CreateUnidadeMedidaCommand(
                request.nome(), request.sigla(), request.fatorConversao()));
        return ApiResponse.created("Unidade de medida criada!", new CreatedResponse(result.id())).toEntity();
    }

}
