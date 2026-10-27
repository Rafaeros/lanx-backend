package br.lanx.api.comercial.relatorios.carteira;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import br.lanx.api.comercial.relatorios.carteira.dtos.CarteiraResponse;
import br.lanx.api.shared.web.planilha.Planilha;

/**
 * Planilha da carteira: aba "Carteira" (um pedido por linha, previsão vencida em
 * vermelho e total que acompanha o filtro) e aba "Materiais pendentes" (um
 * material por linha, com o pedido ao lado, para filtrar por material ou OP).
 */
final class CarteiraPlanilha {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter BR_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private static final List<String> COLUNAS_CARTEIRA = List.of("Negociação", "Pedido cliente", "OP",
            "Tipo de serviço", "Cliente", "Código", "Produto", "Previsão", "Qtde pendente", "Valor total",
            "Etapa atual", "Materiais pendentes", "Situação");

    private static final List<String> COLUNAS_MATERIAIS = List.of("Negociação", "OP", "Cliente", "Produto",
            "Previsão do pedido", "Etapa atual", "Material", "Descrição do material", "Qtde pendente", "UM",
            "Previsão do material");

    private CarteiraPlanilha() {
    }

    static byte[] gerar(CarteiraResponse carteira) {
        try (var planilha = new Planilha()) {
            carteira(planilha, carteira);
            materiais(planilha, carteira);
            return planilha.toBytes();
        }
    }

    private static void carteira(Planilha planilha, CarteiraResponse c) {
        XSSFSheet aba = planilha.aba("Carteira");
        int linha = planilha.titulo(aba, "Carteira de pedidos", subtitulo(c));
        int cabecalho = linha;
        planilha.cabecalho(aba, cabecalho, COLUNAS_CARTEIRA, cabecalho + c.itens().size());
        planilha.larguras(aba, 12, 16, 13, 16, 42, 18, 52, 12, 13, 17, 22, 12, 11);
        planilha.logo(aba, 11);
        planilha.paisagem(aba, cabecalho);

        for (CarteiraResponse.Item item : c.itens()) {
            Row row = aba.createRow(++linha);
            planilha.codigo(row, 0, item.negociacao());
            planilha.texto(row, 1, item.pedidoCliente());
            planilha.texto(row, 2, item.ordem());
            planilha.texto(row, 3, item.tipoServico());
            planilha.texto(row, 4, item.cliente());
            planilha.texto(row, 5, item.codigo());
            planilha.texto(row, 6, item.descricao());
            planilha.data(row, 7, item.previsao(), item.atrasado());
            planilha.decimal(row, 8, item.pendente());
            planilha.moeda(row, 9, item.valorTotal());
            planilha.texto(row, 10, item.etapaAtual());
            planilha.inteiro(row, 11, item.materiaisPendentes().size());
            planilha.texto(row, 12, situacao(item));
        }

        if (!c.itens().isEmpty()) {
            Row total = aba.createRow(linha + 1);
            planilha.negrito(total, 8, "Total");
            // SUBTOTAL(109) soma só as linhas visíveis: o total acompanha o filtro automático
            planilha.moedaTotal(total, 9, "SUBTOTAL(109,J" + (cabecalho + 2) + ":J" + (linha + 1) + ")");
        }
    }

    private static void materiais(Planilha planilha, CarteiraResponse c) {
        XSSFSheet aba = planilha.aba("Materiais pendentes");
        long quantidade = c.itens().stream().mapToLong(i -> i.materiaisPendentes().size()).sum();
        int linha = planilha.titulo(aba, "Materiais pendentes da carteira",
                periodo(c) + " · " + quantidade + " materiais · em vermelho: material previsto para depois da"
                        + " entrega do pedido ou com previsão vencida");
        int cabecalho = linha;
        planilha.cabecalho(aba, cabecalho, COLUNAS_MATERIAIS, cabecalho + (int) quantidade);
        planilha.larguras(aba, 12, 13, 40, 18, 12, 22, 18, 56, 13, 7, 12);
        planilha.logo(aba, 9);
        planilha.paisagem(aba, cabecalho);

        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        for (CarteiraResponse.Item item : c.itens()) {
            for (CarteiraResponse.MaterialPendente material : item.materiaisPendentes()) {
                Row row = aba.createRow(++linha);
                planilha.codigo(row, 0, item.negociacao());
                planilha.texto(row, 1, item.ordem());
                planilha.texto(row, 2, item.cliente());
                planilha.texto(row, 3, item.codigo());
                planilha.data(row, 4, item.previsao(), item.atrasado());
                planilha.texto(row, 5, item.etapaAtual());
                planilha.texto(row, 6, material.codigo());
                planilha.texto(row, 7, material.descricao());
                planilha.decimal(row, 8, material.quantidadePendente());
                planilha.texto(row, 9, material.unidadeMedida());
                planilha.data(row, 10, material.previsao(), materialEmRisco(material, item, hoje));
            }
        }
    }

    /** Material que chega depois da entrega do pedido, ou cuja previsão já passou. */
    static boolean materialEmRisco(CarteiraResponse.MaterialPendente material, CarteiraResponse.Item item,
            LocalDate hoje) {
        LocalDate previsao = material.previsao();
        if (previsao == null) {
            return false;
        }
        return previsao.isBefore(hoje) || (item.previsao() != null && previsao.isAfter(item.previsao()));
    }

    private static String situacao(CarteiraResponse.Item item) {
        if (item.previsao() == null) {
            return "Sem previsão";
        }
        return item.atrasado() ? "Atrasado" : "No prazo";
    }

    private static String subtitulo(CarteiraResponse c) {
        var r = c.resumo();
        return periodo(c) + " · " + r.itens() + (r.itens() == 1 ? " item" : " itens") + " · "
                + moeda(r.valorTotal()) + " · " + r.atrasados() + (r.atrasados() == 1 ? " atrasado" : " atrasados")
                + " · " + r.comMaterialPendente() + " com material pendente · dados do ERP de " + BR_HORA.format(c.consultadoEm());
    }

    private static String periodo(CarteiraResponse c) {
        return "Período de " + c.dataInicio().format(BR) + " a " + c.dataFim().format(BR);
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor);
    }
}
