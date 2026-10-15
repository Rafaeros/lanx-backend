package br.lanx.api.compras.relatorios.analisepedidos.adapter;

import java.util.UUID;
import br.lanx.api.cadastros.materiais.dtos.CreateMaterialRequest;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.materiais.MaterialService;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeMaterialPort;

@Component
public class MaterialNormalizerAdapter implements NormalizeMaterialPort {

    private final MaterialService materialService;

    public MaterialNormalizerAdapter(MaterialService materialService) {
        this.materialService = materialService;
    }

    @Override
    public UUID obterOuCriar(String codigo, String descricao, UUID unidadeMedidaId) {
        var createRequest = new CreateMaterialRequest(codigo, descricao, unidadeMedidaId);

        var result = materialService.findByCodigoOrCreate(createRequest);

        return result.material().id();
    }

}
