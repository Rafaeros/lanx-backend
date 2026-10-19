package br.lanx.api.compras.relatorios.iqf.adapter.in.web;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetLineDash;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFPresetLineDash;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfFornecedor;
import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfMes;
import br.lanx.api.compras.relatorios.iqf.application.dto.ResultadoIqfPeriodo;
import br.lanx.api.compras.relatorios.iqf.domain.ClassificacaoIqf;
import br.lanx.api.compras.relatorios.iqf.domain.MetaIqf;
import br.lanx.api.shared.web.planilha.Planilha;

/**
 * Planilha do IQF: aba "Resumo" (indicadores, IQF mês a mês e gráfico nativo do
 * Excel com as metas A e B) e aba "Fornecedores" (piores primeiro).
 */
final class IqfPlanilha {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] MESES = { "jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out",
            "nov", "dez" };
    private static final double META = MetaIqf.GERAL_MENSAL.doubleValue();

    private IqfPlanilha() {
    }

    static byte[] gerar(ResultadoIqfPeriodo resultado) {
        try (var planilha = new Planilha()) {
            resumo(planilha, resultado);
            fornecedores(planilha, resultado);
            return planilha.toBytes();
        }
    }

    private static void resumo(Planilha planilha, ResultadoIqfPeriodo r) {
        XSSFSheet aba = planilha.aba("Resumo");
        int linha = planilha.titulo(aba, "IQF de fornecedores (prazo)", subtitulo(r));
        planilha.larguras(aba, 20, 12, 12, 12, 12, 18, 14);

        linha = indicador(planilha, aba, linha, "IQF geral", r.percentualNoPrazo(), true);
        linha = indicador(planilha, aba, linha, "Classificação", null, false);
        planilha.texto(aba.getRow(linha - 1), 1, faixa(r.percentualNoPrazo()));
        linha = indicador(planilha, aba, linha, "Entregas avaliadas", BigDecimal.valueOf(r.entregasAvaliadas()), false);
        linha = indicador(planilha, aba, linha, "No prazo", BigDecimal.valueOf(r.entregasNoPrazo()), false);
        linha = indicador(planilha, aba, linha, "Atrasadas", BigDecimal.valueOf(r.entregasAtrasadas()), false);
        linha = indicador(planilha, aba, linha, "Meta mensal", MetaIqf.GERAL_MENSAL, true);
        linha++;

        // tabela mensal: também é a fonte de dados do gráfico (metas em colunas próprias)
        int cabecalho = linha;
        List<ResultadoIqfMes> meses = r.meses();
        planilha.cabecalho(aba, cabecalho,
                List.of("Mês", "Avaliadas", "No prazo", "Atrasadas", "IQF (%)", "Classificação", "Meta 97%", "Meta"),
                cabecalho + meses.size());
        aba.setColumnHidden(7, true);
        aba.createFreezePane(0, 0); // o congelamento só faz sentido na aba de fornecedores

        for (ResultadoIqfMes mes : meses) {
            Row row = aba.createRow(++linha);
            planilha.texto(row, 0, rotulo(mes.mes()));
            planilha.inteiro(row, 1, mes.entregasAvaliadas());
            planilha.inteiro(row, 2, mes.entregasNoPrazo());
            planilha.inteiro(row, 3, mes.entregasAtrasadas());
            planilha.percentual(row, 4, mes.percentualNoPrazo());
            planilha.texto(row, 5, mes.percentualNoPrazo() == null ? "Sem avaliação" : faixa(mes.percentualNoPrazo()));
            Boolean atingiu = MetaIqf.atingiu(mes.percentualNoPrazo(), MetaIqf.GERAL_MENSAL);
            planilha.texto(row, 6, atingiu == null ? "-" : atingiu ? "Atingida" : "Abaixo");
            row.createCell(7).setCellValue(META);
        }

        if (!meses.isEmpty()) {
            grafico(aba, cabecalho + 1, linha);
        }
    }

    private static int indicador(Planilha planilha, XSSFSheet aba, int linha, String rotulo, BigDecimal valor,
            boolean percentual) {
        Row row = aba.createRow(linha);
        planilha.negrito(row, 0, rotulo);
        if (percentual) {
            planilha.percentual(row, 1, valor);
            if (valor == null) {
                planilha.texto(row, 1, "-");
            }
        } else if (valor != null) {
            planilha.inteiro(row, 1, valor);
        }
        return linha + 1;
    }

    /** Colunas do IQF mensal com a linha da meta mensal (97%), à direita da tabela. */
    private static void grafico(XSSFSheet aba, int primeira, int ultima) {
        var drawing = aba.createDrawingPatriarch();
        int altura = Math.max(18, ultima - primeira + 6);
        var anchor = drawing.createAnchor(0, 0, 0, 0, 8, 2, 20, 2 + altura);
        var chart = drawing.createChart(anchor);
        chart.setTitleText("IQF mensal (% de entregas no prazo)");
        chart.setTitleOverlay(false);
        chart.setPlotOnlyVisibleCells(false); // as metas ficam em colunas ocultas
        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis eixoMes = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis eixoIqf = chart.createValueAxis(AxisPosition.LEFT);
        eixoIqf.setMinimum(0);
        eixoIqf.setMaximum(100);
        eixoIqf.setMajorUnit(25);
        eixoIqf.setCrosses(AxisCrosses.AUTO_ZERO);

        var categorias = XDDFDataSourcesFactory.fromStringCellRange(aba, new CellRangeAddress(primeira, ultima, 0, 0));
        var iqf = XDDFDataSourcesFactory.fromNumericCellRange(aba, new CellRangeAddress(primeira, ultima, 4, 4));
        var meta = XDDFDataSourcesFactory.fromNumericCellRange(aba, new CellRangeAddress(primeira, ultima, 7, 7));

        var barras = (XDDFBarChartData) chart.createData(ChartTypes.BAR, eixoMes, eixoIqf);
        barras.setBarDirection(BarDirection.COL);
        barras.setVaryColors(false);
        barras.setGapWidth(60);
        XDDFChartData.Series serieIqf = barras.addSeries(categorias, iqf);
        serieIqf.setTitle("IQF (%)", null);
        serieIqf.setShapeProperties(preenchimento("7609E8"));
        chart.plot(barras);

        var linhas = (XDDFLineChartData) chart.createData(ChartTypes.LINE, eixoMes, eixoIqf);
        linhas.setVaryColors(false);
        meta((XDDFLineChartData.Series) linhas.addSeries(categorias, meta), "Meta mensal (97%)", "047857");
        chart.plot(linhas);
    }

    private static void meta(XDDFLineChartData.Series serie, String titulo, String cor) {
        serie.setTitle(titulo, null);
        serie.setSmooth(false);
        serie.setMarkerStyle(MarkerStyle.NONE);
        var linha = new XDDFLineProperties();
        linha.setFillProperties(new XDDFSolidFillProperties(XDDFColor.from(hex(cor))));
        linha.setPresetDash(new XDDFPresetLineDash(PresetLineDash.DASH));
        linha.setWidth(1.5);
        var props = new XDDFShapeProperties();
        props.setLineProperties(linha);
        serie.setShapeProperties(props);
    }

    private static XDDFShapeProperties preenchimento(String cor) {
        var props = new XDDFShapeProperties();
        props.setFillProperties(new XDDFSolidFillProperties(XDDFColor.from(hex(cor))));
        return props;
    }

    private static byte[] hex(String rgb) {
        return new byte[] {
                (byte) Integer.parseInt(rgb.substring(0, 2), 16),
                (byte) Integer.parseInt(rgb.substring(2, 4), 16),
                (byte) Integer.parseInt(rgb.substring(4, 6), 16) };
    }

    private static void fornecedores(Planilha planilha, ResultadoIqfPeriodo r) {
        XSSFSheet aba = planilha.aba("Fornecedores");
        int linha = planilha.titulo(aba, "IQF por fornecedor", subtitulo(r));
        List<ResultadoIqfFornecedor> ordenados = r.fornecedores().stream()
                .sorted(Comparator
                        .comparing(ResultadoIqfFornecedor::percentualNoPrazo,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ResultadoIqfFornecedor::entregasAvaliadas, Comparator.reverseOrder())
                        .thenComparing(ResultadoIqfFornecedor::fornecedor,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        planilha.cabecalho(aba, linha,
                List.of("Fornecedor", "Avaliadas", "No prazo", "Atrasadas", "IQF (%)", "Classificação", "Meta 85%"),
                linha + ordenados.size());
        planilha.larguras(aba, 48, 12, 12, 12, 12, 22, 14);

        for (ResultadoIqfFornecedor f : ordenados) {
            Row row = aba.createRow(++linha);
            planilha.texto(row, 0, f.fornecedor());
            planilha.inteiro(row, 1, f.entregasAvaliadas());
            planilha.inteiro(row, 2, f.entregasNoPrazo());
            planilha.inteiro(row, 3, f.entregasAtrasadas());
            planilha.percentual(row, 4, f.percentualNoPrazo());
            planilha.texto(row, 5, f.percentualNoPrazo() == null ? "Não avaliado" : faixa(f.percentualNoPrazo()));
            Boolean naMeta = MetaIqf.atingiu(f.percentualNoPrazo(), MetaIqf.FORNECEDOR);
            planilha.texto(row, 6, naMeta == null ? "-" : naMeta ? "Atingida" : "Abaixo");
        }
    }

    private static String subtitulo(ResultadoIqfPeriodo r) {
        String rateios = r.rateios().isEmpty() ? "todos os rateios"
                : "rateios: " + String.join(", ", r.rateios().stream().sorted().toList());
        return "Entrega prevista de " + r.inicioPeriodo().format(BR) + " a " + r.fimPeriodo().format(BR)
                + " · " + rateios + " · meta geral 97%, por fornecedor 85% · tolerância de "
                + r.toleranciaDias() + (r.toleranciaDias() == 1 ? " dia" : " dias") + " após a previsão";
    }

    private static String faixa(BigDecimal percentual) {
        if (percentual == null) {
            return "-";
        }
        return switch (ClassificacaoIqf.de(percentual)) {
            case A -> "A · Qualificado";
            case B -> "B · Em atenção";
            case C -> "C · Crítico";
        };
    }

    private static String rotulo(YearMonth mes) {
        return MESES[mes.getMonthValue() - 1] + "/" + mes.getYear();
    }
}
