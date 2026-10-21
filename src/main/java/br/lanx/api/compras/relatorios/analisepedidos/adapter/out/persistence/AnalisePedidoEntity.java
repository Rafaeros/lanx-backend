package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.lanx.api.cadastros.fornecedores.Fornecedor;
import br.lanx.api.cadastros.materiais.Material;
import br.lanx.api.cadastros.unidademedida.adapter.out.persistence.UnidadeMedidaEntity;
import br.lanx.api.shared.domain.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(schema = "compras", name = "analise_pedidos_itens")
public class AnalisePedidoEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_medida_id", nullable = false)
    private UnidadeMedidaEntity unidadeMedida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "requisicao_de_compra")
    private String requisicaoDeCompra;

    @Column(name = "solicitante")
    private String solicitante;

    @Column(name = "data_emissao_rc")
    private LocalDate dataEmissaoRc;

    @Column(name = "data_previsao_entrega_rc")
    private LocalDate dataPrevisaoEntregaRc;

    @Column(name = "pedido_de_compra")
    private String pedidoDeCompra;

    @Column(name = "comprador")
    private String comprador;

    @Column(name = "data_emissao_pc")
    private LocalDate dataEmissaoPc;

    @Column(name = "data_previsao_entrega_pc")
    private LocalDate dataPrevisaoEntregaPc;

    @Column(name = "data_entrega_pc")
    private LocalDate dataEntregaPc;

    @Column(name = "quantidade_solicitada")
    private BigDecimal quantidadeSolicitada;

    @Column(name = "quantidade_atendida")
    private BigDecimal quantidadeAtendida;

    @Column(name = "saldo")
    private BigDecimal saldo;

    @Column(name = "local_")
    private String local;

    @Column(name = "valor_unitario")
    private BigDecimal valorUnitario;

    @Column(name = "valor_frete")
    private BigDecimal valorFrete;

    @Column(name = "ipi")
    private BigDecimal ipi;

    @Column(name = "valor_total")
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

    protected AnalisePedidoEntity() {
    }

    @PrePersist
    private void generateId() {
        if (id == null) {
            id = Ids.newId();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public UnidadeMedidaEntity getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedidaEntity unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
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

}