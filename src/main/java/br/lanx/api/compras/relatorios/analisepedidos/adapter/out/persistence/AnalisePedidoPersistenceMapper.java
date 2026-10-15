package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import org.springframework.stereotype.Component;

@Component
public class AnalisePedidoPersistenceMapper {

    public AnalisePedidoEntity toEntity(ItemAnalisePedido item) {
        if (item == null) {
            return null;
        }

        AnalisePedidoEntity entity = new AnalisePedidoEntity();
        entity.setId(item.id());
        entity.setMaterialId(item.materialId());
        entity.setUnidadeMedidaId(item.unidadeMedidaId());
        entity.setRequisicaoDeCompra(item.requisicaoDeCompra());
        entity.setSolicitante(item.solicitante());
        entity.setDataEmissaoRc(item.dataEmissaoRc());
        entity.setDataPrevisaoEntregaRc(item.dataPrevisaoEntregaRc());
        entity.setPedidoDeCompra(item.pedidoDeCompra());
        entity.setComprador(item.comprador());
        entity.setDataEmissaoPc(item.dataEmissaoPc());
        entity.setDataPrevisaoEntregaPc(item.dataPrevisaoEntregaPc());
        entity.setDataEntregaPc(item.dataEntregaPc());
        entity.setFornecedor(item.fornecedor());
        entity.setPais(item.pais());
        entity.setCodigoItem(item.codigoItem());
        entity.setDescricaoItem(item.descricaoItem());
        entity.setUnidadeMedida(item.unidadeMedida());
        entity.setQuantidadeSolicitada(item.quantidadeSolicitada());
        entity.setQuantidadeAtendida(item.quantidadeAtendida());
        entity.setSaldo(item.saldo());
        entity.setLocal(item.local());
        entity.setValorUnitario(item.valorUnitario());
        entity.setValorFrete(item.valorFrete());
        entity.setIpi(item.ipi());
        entity.setValorTotal(item.valorTotal());
        entity.setCondicaoPagamento(item.condicaoPagamento());
        entity.setNcm(item.ncm());
        entity.setRecebimento(item.recebimento());
        entity.setNotaFiscal(item.notaFiscal());
        entity.setQuantidadeRecebimentos(item.quantidadeRecebimentos());
        entity.setRateio(item.rateio());
        entity.setReferencia(item.referencia());
        entity.setInvoice(item.invoice());
        entity.setResponsavelCompras(item.responsavelCompras());
        entity.setObservacao(item.observacao());

        return entity;
    }

    public ItemAnalisePedido toDomain(AnalisePedidoEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ItemAnalisePedido(
            entity.getId(),
            entity.getMaterialId(),
            entity.getUnidadeMedidaId(),
            entity.getRequisicaoDeCompra(),
            entity.getSolicitante(),
            entity.getDataEmissaoRc(),
            entity.getDataPrevisaoEntregaRc(),
            entity.getPedidoDeCompra(),
            entity.getComprador(),
            entity.getDataEmissaoPc(),
            entity.getDataPrevisaoEntregaPc(),
            entity.getDataEntregaPc(),
            entity.getFornecedor(),
            entity.getPais(),
            entity.getCodigoItem(),
            entity.getDescricaoItem(),
            entity.getUnidadeMedida(),
            entity.getQuantidadeSolicitada(),
            entity.getQuantidadeAtendida(),
            entity.getSaldo(),
            entity.getLocal(),
            entity.getValorUnitario(),
            entity.getValorFrete(),
            entity.getIpi(),
            entity.getValorTotal(),
            entity.getCondicaoPagamento(),
            entity.getNcm(),
            entity.getRecebimento(),
            entity.getNotaFiscal(),
            entity.getQuantidadeRecebimentos(),
            entity.getRateio(),
            entity.getReferencia(),
            entity.getInvoice(),
            entity.getResponsavelCompras(),
            entity.getObservacao()
        );
    }
}