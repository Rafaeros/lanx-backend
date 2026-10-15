package br.lanx.api.compras.relatorios.analisepedidos.application.port.out;

import java.util.UUID;

public interface NormalizeMaterialPort {
    UUID obterOuCriar(String codigo, String descricao, UUID unidadeMedidaId);
}
