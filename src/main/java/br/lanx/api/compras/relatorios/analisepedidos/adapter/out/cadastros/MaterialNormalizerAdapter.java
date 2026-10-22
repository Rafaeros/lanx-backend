package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.cadastros;

import java.util.UUID;
import br.lanx.api.cadastros.materiais.dtos.CreateMaterialRequest;
import br.lanx.api.cadastros.materiais.enums.TipoMaterial;

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
        // Itens comprados entram como matéria-prima; um material existente mantém o tipo que já tem
        var createRequest = new CreateMaterialRequest(codigo, descricao, unidadeMedidaId, TipoMaterial.MATERIA_PRIMA);

        var result = materialService.findByCodigoOrCreate(createRequest);

        return result.material().id();
    }

}
