package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeFiltro;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeIndicadoresResponse;
import br.lanx.api.testeinspecao.operacoes.naoconformidade.dtos.NaoConformidadeResponse;
import br.lanx.api.shared.web.planilha.Planilha;

/**
 * Planilha de não conformidades: aba com uma NC por linha e os dados da OP ao
 * lado (o layout da planilha que a qualidade preenchia à mão, com o
 * departamento no lugar do número da operação), mais uma aba por agrupamento
 * dos indicadores (departamento, tipo, cliente e mês).
 */
final class NaoConformidadePlanilha {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String> COLUNAS = List.of("OP", "Descrição", "Cliente", "Data entrega", "Qtd OP",
            "Qtd não conforme", "Não conformidade", "Departamento", "Data não conformidade");

    private NaoConformidadePlanilha() {
    }

    static byte[] gerar(List<NaoConformidadeResponse> itens, NaoConformidadeIndicadoresResponse indicadores,
            NaoConformidadeFiltro filtro) {
        try (var planilha = new Planilha()) {
            registros(planilha, itens, filtro);
            String sub = subtitulo(itens, filtro);
            grupos(planilha, "Por departamento", "Departamento", indicadores.porDepartamento(), sub);
            grupos(planilha, "Por não conformidade", "Não conformidade", indicadores.porTipo(), sub);
            grupos(planilha, "Por cliente", "Cliente", indicadores.porCliente(), sub);
            meses(planilha, indicadores.meses(), sub);
            return planilha.toBytes();
        }
    }

    private static void registros(Planilha planilha, List<NaoConformidadeResponse> itens,
            NaoConformidadeFiltro filtro) {
        XSSFSheet aba = planilha.aba("Não conformidades");
        int linha = planilha.titulo(aba, "Não conformidades", subtitulo(itens, filtro));
        int cabecalho = linha;
        planilha.cabecalho(aba, cabecalho, COLUNAS, cabecalho + itens.size());
        planilha.larguras(aba, 13, 22, 30, 13, 10, 12, 30, 24, 14);
        planilha.logo(aba, 7);
        planilha.paisagem(aba, cabecalho);

        for (NaoConformidadeResponse nc : itens) {
            Row row = aba.createRow(++linha);
            var op = nc.ordemDeProducao();
            if (op != null) {
                planilha.texto(row, 0, op.codigo());
                planilha.texto(row, 1, op.materialCodigo());
                planilha.texto(row, 2, op.clienteNome());
                planilha.data(row, 3, op.previsao());
                planilha.decimal(row, 4, op.quantidade());
            }
            planilha.decimal(row, 5, nc.quantidadeNaoConforme());
            planilha.texto(row, 6, nc.tipo() != null ? nc.tipo().nome() : null);
            planilha.texto(row, 7, nc.departamento() != null ? nc.departamento().nome() : null);
            planilha.data(row, 8, nc.dataNaoConformidade());
        }
    }

    /** Uma aba por agrupamento, do maior para o menor, com o % acumulado (Pareto). */
    private static void grupos(Planilha planilha, String nomeAba, String coluna,
            List<NaoConformidadeIndicadoresResponse.Grupo> grupos, String subtitulo) {
        XSSFSheet aba = planilha.aba(nomeAba);
        int linha = planilha.titulo(aba, "Não conformidades " + nomeAba.toLowerCase(), subtitulo);
        int cabecalho = linha;
        planilha.cabecalho(aba, cabecalho, List.of(coluna, "Registros", "Qtd não conforme", "% do total",
                "% acumulado"), cabecalho + grupos.size());
        planilha.larguras(aba, 36, 11, 15, 11, 12);
        planilha.logo(aba, 3);
        for (var grupo : grupos) {
            Row row = aba.createRow(++linha);
            planilha.texto(row, 0, grupo.nome());
            planilha.inteiro(row, 1, grupo.registros());
            planilha.decimal(row, 2, grupo.quantidade());
            planilha.percentual(row, 3, grupo.percentual());
            planilha.percentual(row, 4, grupo.percentualAcumulado());
        }
    }

    private static void meses(Planilha planilha, List<NaoConformidadeIndicadoresResponse.Mes> meses,
            String subtitulo) {
        XSSFSheet aba = planilha.aba("Mês a mês");
        int linha = planilha.titulo(aba, "Não conformidades mês a mês", subtitulo);
        int cabecalho = linha;
        planilha.cabecalho(aba, cabecalho, List.of("Mês", "Registros", "Qtd não conforme"),
                cabecalho + meses.size());
        planilha.larguras(aba, 12, 11, 15);
        planilha.logo(aba, 2);
        for (var mes : meses) {
            Row row = aba.createRow(++linha);
            planilha.texto(row, 0, mes.mes());
            planilha.inteiro(row, 1, mes.registros());
            planilha.decimal(row, 2, mes.quantidade());
        }
    }

    private static String subtitulo(List<NaoConformidadeResponse> itens, NaoConformidadeFiltro filtro) {
        String periodo;
        if (filtro.dataInicio() != null && filtro.dataFim() != null) {
            periodo = "Período de " + filtro.dataInicio().format(BR) + " a " + filtro.dataFim().format(BR);
        } else if (filtro.dataInicio() != null) {
            periodo = "A partir de " + filtro.dataInicio().format(BR);
        } else if (filtro.dataFim() != null) {
            periodo = "Até " + filtro.dataFim().format(BR);
        } else {
            periodo = "Todo o período";
        }
        String busca = filtro.busca() != null && !filtro.busca().isBlank()
                ? " · busca \"" + filtro.busca().trim() + "\""
                : "";
        return periodo + busca + " · " + itens.size() + (itens.size() == 1 ? " registro" : " registros");
    }
}
