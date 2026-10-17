package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import br.lanx.api.cadastros.fornecedores.Fornecedor;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.cadastros.unidademedida.adapter.out.persistence.UnidadeMedidaEntity;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import org.springframework.stereotype.Component;

@Component
public class AnalisePedidoPersistenceMapper {

    public AnalisePedidoEntity toEntity(ItemAnalisePedido item,
            Material material,
            UnidadeMedidaEntity unidadeMedida,
            Fornecedor fornecedor) {
        if (item == null) {
            return null;
        }

        AnalisePedidoEntity entity = new AnalisePedidoEntity();
        entity.setId(item.id());
        entity.setMaterial(material);
        entity.setUnidadeMedida(unidadeMedida);
        entity.setFornecedor(fornecedor);

        entity.setRequisicaoDeCompra(item.requisicaoDeCompra());
        entity.setSolicitante(item.solicitante());
        entity.setDataEmissaoRc(item.dataEmissaoRc());
        entity.setDataPrevisaoEntregaRc(item.dataPrevisaoEntregaRc());
        entity.setPedidoDeCompra(item.pedidoDeCompra());
        entity.setComprador(item.comprador());
        entity.setDataEmissaoPc(item.dataEmissaoPc());
        entity.setDataPrevisaoEntregaPc(item.dataPrevisaoEntregaPc());
        entity.setDataEntregaPc(item.dataEntregaPc());
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

        Material material = entity.getMaterial();
        UnidadeMedidaEntity unidadeMedida = entity.getUnidadeMedida();
        Fornecedor fornecedor = entity.getFornecedor();

        return new ItemAnalisePedido(
                entity.getId(),
                material != null ? material.getId() : null,
                unidadeMedida != null ? unidadeMedida.getId() : null,
                fornecedor != null ? fornecedor.getId() : null,
                entity.getRequisicaoDeCompra(),
                entity.getSolicitante(),
                entity.getDataEmissaoRc(),
                entity.getDataPrevisaoEntregaRc(),
                entity.getPedidoDeCompra(),
                entity.getComprador(),
                entity.getDataEmissaoPc(),
                entity.getDataPrevisaoEntregaPc(),
                entity.getDataEntregaPc(),
                fornecedor != null ? fornecedor.getNome() : null,
                fornecedor != null && fornecedor.getPais() != null
                        ? fornecedor.getPais().getNome()
                        : null,
                material != null ? material.getCodigo() : null,
                material != null ? material.getDescricao() : null,
                unidadeMedida != null ? unidadeMedida.getSigla() : null,
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
                entity.getObservacao());
    }
}