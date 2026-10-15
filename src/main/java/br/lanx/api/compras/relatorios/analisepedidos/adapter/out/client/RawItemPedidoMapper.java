package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class RawItemPedidoMapper {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public ItemAnalisePedido toDomain(RawItemPedido raw) {
        if (raw == null) {
            return null;
        }
        try {

            return new ItemAnalisePedido(
                    raw.requisicaoDeCompra(),
                    raw.solicitante(),
                    parseDate(raw.dataEmissaoRc()),
                    parseDate(raw.dataPrevisaoEntregaRc()),
                    raw.pedidoDeCompra(),
                    raw.comprador(),
                    parseDate(raw.dataEmissaoPc()),
                    parseDate(raw.dataPrevisaoEntregaPc()),
                    parseDate(raw.dataEntregaPc()),
                    raw.fornecedor(),
                    raw.pais(),
                    raw.codigoItem(),
                    raw.descricaoItem(),
                    raw.unidadeMedida(),
                    parseDecimal(raw.quantidadeSolicitada()),
                    parseDecimal(raw.quantidadeAtendida()),
                    parseDecimal(raw.saldo()),
                    raw.local(),
                    parseDecimal(raw.valorUnitario()),
                    parseDecimal(raw.valorFrete()),
                    parseDecimal(raw.ipi()),
                    parseDecimal(raw.valorTotal()),
                    raw.condicaoPagamento(),
                    raw.ncm(),
                    raw.recebimento(),
                    raw.notaFiscal(),
                    raw.quantidadeRecebimentos(),
                    raw.rateio(),
                    raw.referencia(),
                    raw.invoice(),
                    raw.responsavelCompras(),
                    raw.observacao());
        } catch (Exception e) {
            System.err.println(">>> Erro ao converter item: " + raw.codigoItem() + " - " + e.getMessage());
            throw e;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida: " + value, e);
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido: " + value, e);
        }
    }
}