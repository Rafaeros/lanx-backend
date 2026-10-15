package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "compras", name = "analise_pedido_item")
@SQLDelete(sql = "UPDATE compras.analise_pedido_item SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class AnalisePedidoEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "unidade_medida_id", nullable = false)
    private UUID unidadeMedidaId;

    @Column(name = "requisicao_de_compra")
    private String requisicaoDeCompra;

    @Column(name = "solicitante")
    private String solicitante;

    @Column(name = "data_emissao_rc")
    private LocalDate dataEmissaoRc;

    @Column(name = "data_previsao_entrega_rc")
    private LocalDate dataPrevisaoEntregaRc;

    @Column(name = "pedido_de_compra", nullable = false)
    private String pedidoDeCompra;

    @Column(name = "comprador")
    private String comprador;

    @Column(name = "data_emissao_pc")
    private LocalDate dataEmissaoPc;

    @Column(name = "data_previsao_entrega_pc")
    private LocalDate dataPrevisaoEntregaPc;

    @Column(name = "data_entrega_pc")
    private LocalDate dataEntregaPc;

    @Column(name = "fornecedor")
    private String fornecedor;

    @Column(name = "pais")
    private String pais;

    @Column(name = "codigo_item")
    private String codigoItem;

    @Column(name = "descricao_item")
    private String descricaoItem;

    @Column(name = "unidade_medida")
    private String unidadeMedida;

    @Column(name = "quantidade_solicitada", precision = 19, scale = 4)
    private BigDecimal quantidadeSolicitada;

    @Column(name = "quantidade_atendida", precision = 19, scale = 4)
    private BigDecimal quantidadeAtendida;

    @Column(name = "saldo", precision = 19, scale = 4)
    private BigDecimal saldo;

    @Column(name = "local")
    private String local;

    @Column(name = "valor_unitario", precision = 19, scale = 4)
    private BigDecimal valorUnitario;

    @Column(name = "valor_frete", precision = 19, scale = 4)
    private BigDecimal valorFrete;

    @Column(name = "ipi", precision = 19, scale = 4)
    private BigDecimal ipi;

    @Column(name = "valor_total", precision = 19, scale = 4)
    private BigDecimal valorTotal;

    @Column(name = "condicao_pagamento")
    private String condicaoPagamento;

    @Column(name = "ncm")
    private String ncm;

    @Column(name = "recebimento")
    private String recebimento;

    @Column(name = "nota_fiscal")
    private String notaFiscal;

    @Column(name = "quantidade_recebimentos")
    private Integer quantidadeRecebimentos;

    @Column(name = "rateio")
    private String rateio;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "invoice")
    private String invoice;

    @Column(name = "responsavel_compras")
    private String responsavelCompras;

    @Column(name = "observacao")
    private String observacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected AnalisePedidoEntity() {
    }

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = br.lanx.api.shared.domain.Ids.newId();
        }
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public void setMaterialId(UUID materialId) {
        this.materialId = materialId;
    }

    public UUID getUnidadeMedidaId() {
        return unidadeMedidaId;
    }

    public void setUnidadeMedidaId(UUID unidadeMedidaId) {
        this.unidadeMedidaId = unidadeMedidaId;
    }

    public String getRequisicaoDeCompra() {
        return requisicaoDeCompra;
    }

    public void setRequisicaoDeCompra(String requisicaoDeCompra) {
        this.requisicaoDeCompra = requisicaoDeCompra;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public LocalDate getDataEmissaoRc() {
        return dataEmissaoRc;
    }

    public void setDataEmissaoRc(LocalDate dataEmissaoRc) {
        this.dataEmissaoRc = dataEmissaoRc;
    }

    public LocalDate getDataPrevisaoEntregaRc() {
        return dataPrevisaoEntregaRc;
    }

    public void setDataPrevisaoEntregaRc(LocalDate dataPrevisaoEntregaRc) {
        this.dataPrevisaoEntregaRc = dataPrevisaoEntregaRc;
    }

    public String getPedidoDeCompra() {
        return pedidoDeCompra;
    }

    public void setPedidoDeCompra(String pedidoDeCompra) {
        this.pedidoDeCompra = pedidoDeCompra;
    }

    public String getComprador() {
        return comprador;
    }

    public void setComprador(String comprador) {
        this.comprador = comprador;
    }

    public LocalDate getDataEmissaoPc() {
        return dataEmissaoPc;
    }

    public void setDataEmissaoPc(LocalDate dataEmissaoPc) {
        this.dataEmissaoPc = dataEmissaoPc;
    }

    public LocalDate getDataPrevisaoEntregaPc() {
        return dataPrevisaoEntregaPc;
    }

    public void setDataPrevisaoEntregaPc(LocalDate dataPrevisaoEntregaPc) {
        this.dataPrevisaoEntregaPc = dataPrevisaoEntregaPc;
    }

    public LocalDate getDataEntregaPc() {
        return dataEntregaPc;
    }

    public void setDataEntregaPc(LocalDate dataEntregaPc) {
        this.dataEntregaPc = dataEntregaPc;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public void setCodigoItem(String codigoItem) {
        this.codigoItem = codigoItem;
    }

    public String getDescricaoItem() {
        return descricaoItem;
    }

    public void setDescricaoItem(String descricaoItem) {
        this.descricaoItem = descricaoItem;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public BigDecimal getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }

    public void setQuantidadeSolicitada(BigDecimal quantidadeSolicitada) {
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public BigDecimal getQuantidadeAtendida() {
        return quantidadeAtendida;
    }

    public void setQuantidadeAtendida(BigDecimal quantidadeAtendida) {
        this.quantidadeAtendida = quantidadeAtendida;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getValorFrete() {
        return valorFrete;
    }

    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public BigDecimal getIpi() {
        return ipi;
    }

    public void setIpi(BigDecimal ipi) {
        this.ipi = ipi;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public void setCondicaoPagamento(String condicaoPagamento) {
        this.condicaoPagamento = condicaoPagamento;
    }

    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
    }

    public String getRecebimento() {
        return recebimento;
    }

    public void setRecebimento(String recebimento) {
        this.recebimento = recebimento;
    }

    public String getNotaFiscal() {
        return notaFiscal;
    }

    public void setNotaFiscal(String notaFiscal) {
        this.notaFiscal = notaFiscal;
    }

    public Integer getQuantidadeRecebimentos() {
        return quantidadeRecebimentos;
    }

    public void setQuantidadeRecebimentos(Integer quantidadeRecebimentos) {
        this.quantidadeRecebimentos = quantidadeRecebimentos;
    }

    public String getRateio() {
        return rateio;
    }

    public void setRateio(String rateio) {
        this.rateio = rateio;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getResponsavelCompras() {
        return responsavelCompras;
    }

    public void setResponsavelCompras(String responsavelCompras) {
        this.responsavelCompras = responsavelCompras;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}