package br.lanx.api.producao.operacoes.ordensdeproducao.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.lanx.api.cadastros.clientes.Cliente;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.producao.operacoes.ordensdeproducao.OrdemDeProducao;

public record OrdemDeProducaoResponse(
        UUID id,
        String codigo,
        ClienteResumo cliente,
        /** Null se o material foi excluído do cadastro depois da OP. */
        MaterialResumo material,
        BigDecimal quantidade,
        LocalDate previsao,
        String etapaAtual) {

    public record ClienteResumo(UUID id, String nome) {
    }

    public record MaterialResumo(UUID id, String codigo, String descricao, String unidadeMedida) {
    }

    public static OrdemDeProducaoResponse fromEntity(OrdemDeProducao op) {
        Cliente cliente = op.getCliente();
        Material material = op.getMaterial();
        return new OrdemDeProducaoResponse(
                op.getId(),
                op.getCodigo(),
                cliente == null ? null : new ClienteResumo(cliente.getId(), cliente.getNome()),
                material == null ? null
                        : new MaterialResumo(material.getId(), material.getCodigo(), material.getDescricao(),
                                material.getUnidadeMedida().getSigla()),
                op.getQuantidade(),
                op.getPrevisao(),
                op.getEtapaAtual());
    }
}
