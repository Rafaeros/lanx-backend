package br.lanx.api.compras.relatorios.analisepedidos.adapter.in.web;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;

import br.lanx.api.compras.relatorios.analisepedidos.domain.ConsultaAnalisePedidos;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;
import br.lanx.api.compras.relatorios.analisepedidos.domain.Situacao;
import br.lanx.api.shared.web.planilha.Planilha;

/** Planilha da análise de pedidos: uma linha por item, com as mesmas colunas do ERP. */
final class AnalisePedidosPlanilha {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String> COLUNAS = List.of(
            "Requisição", "Solicitante", "Emissão RC", "Previsão RC",
            "Pedido", "Comprador", "Emissão PC", "Previsão entrega", "Entrega",
            "Situação", "Lead time (dias)",
            "Fornecedor", "País", "Código item", "Descrição item", "UM",
            "Qtd. solicitada", "Qtd. atendida", "Saldo",
            "Valor unitário", "Frete", "IPI", "Valor total",
            "Cond. pagamento", "NCM", "Local", "Rateio",
            "Recebimento", "Nota fiscal", "Qtd. recebimentos",
            "Referência", "Invoice", "Resp. compras", "Observação");

    private static final int[] LARGURAS = {
            12, 18, 12, 12, 12, 18, 12, 14, 12, 11, 10,
            34, 12, 16, 42, 6, 11, 11, 10,
            14, 12, 12, 15, 18, 12, 14, 28,
            14, 12, 10, 14, 12, 18, 40 };

    private AnalisePedidosPlanilha() {
    }

    static byte[] gerar(ConsultaAnalisePedidos consulta, List<ItemAnalisePedido> itens) {
        try (var planilha = new Planilha()) {
            var aba = planilha.aba("Análise de pedidos");
            int linha = planilha.titulo(aba, "Análise de pedidos",
                    "Emissão do PC de " + consulta.dataInicio().format(BR) + " a " + consulta.dataFim().format(BR)
                            + filtros(consulta) + " · " + itens.size() + " itens");

            planilha.cabecalho(aba, linha, COLUNAS, linha + itens.size());
            planilha.larguras(aba, LARGURAS);

            for (ItemAnalisePedido item : itens) {
                Row row = aba.createRow(++linha);
                int c = 0;
                planilha.texto(row, c++, item.requisicaoDeCompra());
                planilha.texto(row, c++, item.solicitante());
                planilha.data(row, c++, item.dataEmissaoRc());
                planilha.data(row, c++, item.dataPrevisaoEntregaRc());
                planilha.texto(row, c++, item.pedidoDeCompra());
                planilha.texto(row, c++, item.comprador());
                planilha.data(row, c++, item.dataEmissaoPc());
                planilha.data(row, c++, item.dataPrevisaoEntregaPc());
                planilha.data(row, c++, item.dataEntregaPc());
                planilha.texto(row, c++, rotulo(item.situacao()));
                planilha.inteiro(row, c++, item.leadTimeDias());
                planilha.texto(row, c++, item.fornecedor());
                planilha.texto(row, c++, item.pais());
                planilha.texto(row, c++, item.codigoItem());
                planilha.texto(row, c++, item.descricaoItem());
                planilha.texto(row, c++, item.unidadeMedida());
                planilha.decimal(row, c++, item.quantidadeSolicitada());
                planilha.decimal(row, c++, item.quantidadeAtendida());
                planilha.decimal(row, c++, item.saldo());
                planilha.moeda(row, c++, item.valorUnitario());
                planilha.moeda(row, c++, item.valorFrete());
                planilha.moeda(row, c++, item.ipi());
                planilha.moeda(row, c++, item.valorTotal());
                planilha.texto(row, c++, item.condicaoPagamento());
                planilha.texto(row, c++, item.ncm());
                planilha.texto(row, c++, item.local());
                planilha.texto(row, c++, item.rateio());
                planilha.texto(row, c++, item.recebimento());
                planilha.texto(row, c++, item.notaFiscal());
                planilha.inteiro(row, c++, item.quantidadeRecebimentos());
                planilha.texto(row, c++, item.referencia());
                planilha.texto(row, c++, item.invoice());
                planilha.texto(row, c++, item.responsavelCompras());
                planilha.texto(row, c, item.observacao());
            }
            return planilha.toBytes();
        }
    }

    private static String filtros(ConsultaAnalisePedidos consulta) {
        var sb = new StringBuilder();
        if (consulta.temBusca()) {
            sb.append(" · busca \"").append(consulta.busca().trim()).append('"');
        }
        if (consulta.situacao() != null) {
            sb.append(" · situação ").append(rotulo(consulta.situacao()).toLowerCase());
        }
        return sb.toString();
    }

    private static String rotulo(Situacao situacao) {
        return switch (situacao) {
            case PENDENTE -> "Pendente";
            case PARCIAL -> "Parcial";
            case ATENDIDO -> "Atendido";
        };
    }
}
