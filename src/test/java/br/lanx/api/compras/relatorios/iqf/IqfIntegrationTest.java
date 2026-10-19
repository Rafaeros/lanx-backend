package br.lanx.api.compras.relatorios.iqf;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence.AnalisePedidoPersistenceAdapter;
import br.lanx.api.compras.relatorios.analisepedidos.adapter.out.persistence.AnalisePedidoRepository;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeFornecedorPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeMaterialPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeUnidadeMedidaPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

/** IQF de prazo pela API de verdade, a partir dos itens importados da análise de pedidos. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345",
        "app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@ActiveProfiles("test")
class IqfIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
    private static final String BASE = "/compras/relatorios/iqf";

    @Value("${local.server.port}")
    int port;

    @Autowired
    AnalisePedidoPersistenceAdapter persistence;

    @Autowired
    AnalisePedidoRepository repository;

    @Autowired
    NormalizeUnidadeMedidaPort unidades;

    @Autowired
    NormalizeMaterialPort materiais;

    @Autowired
    NormalizeFornecedorPort fornecedores;

    private final HttpClient http = HttpClient.newHttpClient();
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        persistence.salvar(List.of(
                // Aço Tubos: 1 no prazo, 1 atrasada -> 50% (C)
                item("PC-200", "Aço Tubos SA", "IQF-MAT-1", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 9)),
                item("PC-201", "Aço Tubos SA", "IQF-MAT-2", LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 15)),
                // Valvtech: 1 no prazo -> 100% (A)
                item("PC-202", "Valvtech Ltda", "IQF-MAT-3", LocalDate.of(2026, 9, 20), LocalDate.of(2026, 9, 20)),
                // previsão fora do período
                item("PC-203", "Valvtech Ltda", "IQF-MAT-4", LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 30)),
                // outro rateio, em outubro: 1 atrasada
                item("PC-204", "Valvtech Ltda", "IQF-MAT-5", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9),
                        "MATERIAL DE ESCRITÓRIO")));
        admin = login();
    }

    @Test
    void calculaIqfGeralEPorFornecedor() throws Exception {
        var body = get(BASE + "?dataInicio=2026-09-01&dataFim=2026-09-30");

        assertThat(body).contains("\"dataInicio\":\"2026-09-01\"").contains("\"dataFim\":\"2026-09-30\"")
                .contains("\"entregasAvaliadas\":3").contains("\"entregasNoPrazo\":2")
                .contains("\"entregasAtrasadas\":1");
        assertThat(body).contains("\"percentualNoPrazo\":66.67")
                .contains("\"classificacao\":{\"faixa\":\"C\",\"cor\":\"VERMELHO\"}");
        assertThat(body).contains("\"fornecedor\":\"Aço Tubos SA\"").contains("\"percentualNoPrazo\":50.00")
                .contains("\"fornecedor\":\"Valvtech Ltda\"").contains("\"percentualNoPrazo\":100.00")
                .contains("{\"faixa\":\"A\",\"cor\":\"VERDE\"}");
    }

    @Test
    void periodoSemEntregasDevolvePercentualNulo() throws Exception {
        var body = get(BASE + "?dataInicio=2025-01-01&dataFim=2025-01-31");
        // nulos não são serializados: sem entregas avaliadas, não há percentual nem classificação
        assertThat(body).contains("\"entregasAvaliadas\":0").contains("\"fornecedores\":[]")
                .doesNotContain("percentualNoPrazo").doesNotContain("classificacao");
    }

    @Test
    void trazTodosOsMesesDoPeriodoMesmoSemEntregas() throws Exception {
        var body = get(BASE + "?dataInicio=2026-08-01&dataFim=2026-11-30");

        assertThat(body).contains("{\"mes\":\"2026-08\",\"entregasAvaliadas\":1,\"entregasNoPrazo\":0,\"entregasAtrasadas\":1")
                .contains("{\"mes\":\"2026-09\",\"entregasAvaliadas\":3,\"entregasNoPrazo\":2,\"entregasAtrasadas\":1")
                .contains("{\"mes\":\"2026-10\",\"entregasAvaliadas\":1")
                .contains("{\"mes\":\"2026-11\",\"entregasAvaliadas\":0,\"entregasNoPrazo\":0,\"entregasAtrasadas\":0}");
    }

    @Test
    void filtraPorRateios() throws Exception {
        var materiaPrima = get(BASE + "?dataInicio=2026-08-01&dataFim=2026-10-31&rateios=MATERIA-PRIMA");
        assertThat(materiaPrima).contains("\"rateios\":[\"MATERIA-PRIMA\"]").contains("\"entregasAvaliadas\":4");

        var escritorio = get(BASE + "?dataInicio=2026-08-01&dataFim=2026-10-31&rateios="
                + java.net.URLEncoder.encode("MATERIAL DE ESCRITÓRIO", java.nio.charset.StandardCharsets.UTF_8));
        assertThat(escritorio).contains("\"entregasAvaliadas\":1").contains("\"entregasAtrasadas\":1");

        var ambos = get(BASE + "?dataInicio=2026-08-01&dataFim=2026-10-31&rateios=MATERIA-PRIMA&rateios="
                + java.net.URLEncoder.encode("MATERIAL DE ESCRITÓRIO", java.nio.charset.StandardCharsets.UTF_8));
        assertThat(ambos).contains("\"entregasAvaliadas\":5");
    }

    @Test
    void listaRateiosDisponiveis() throws Exception {
        assertThat(get(BASE + "/rateios")).contains("[\"MATERIA-PRIMA\",\"MATERIAL DE ESCRITÓRIO\"]");
    }

    @Test
    void exportaPlanilhaComGrafico() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + BASE
                + "/exportar?dataInicio=2026-08-01&dataFim=2026-10-31"))
                .header("Authorization", "Bearer " + admin).GET().build();
        var response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Disposition").orElse(""))
                .contains("iqf-fornecedores_2026-08-01_2026-10-31.xlsx");
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(
                new java.io.ByteArrayInputStream(response.body()))) {
            assertThat(workbook.getSheet("Resumo")).isNotNull();
            assertThat(workbook.getSheet("Fornecedores").getLastRowNum()).isGreaterThanOrEqualTo(4);
            assertThat(workbook.getSheet("Resumo").getDrawingPatriarch().getCharts()).hasSize(1);
        }
    }

    @Test
    void calculaSerieMensalDeUmFornecedor() throws Exception {
        var geral = get(BASE + "?dataInicio=2026-08-01&dataFim=2026-10-31");
        Matcher m = Pattern.compile("\"fornecedorId\":\"([^\"]+)\",\"fornecedor\":\"Valvtech Ltda\"").matcher(geral);
        assertThat(m.find()).isTrue();

        var body = get(BASE + "/fornecedores/" + m.group(1) + "?dataInicio=2026-08-01&dataFim=2026-10-31");
        // Valvtech: ago atrasada, set no prazo, out atrasada; nada da Aço Tubos
        assertThat(body).contains("\"entregasAvaliadas\":3").contains("\"entregasNoPrazo\":1")
                .contains("{\"mes\":\"2026-08\",\"entregasAvaliadas\":1,\"entregasNoPrazo\":0")
                .contains("{\"mes\":\"2026-09\",\"entregasAvaliadas\":1,\"entregasNoPrazo\":1")
                .doesNotContain("Aço Tubos")
                // na visão do fornecedor, a meta é 85%
                .contains("\"metaMensal\":85");
    }

    @Test
    void informaMetaMensal() throws Exception {
        var body = get(BASE + "?dataInicio=2026-09-01&dataFim=2026-09-30");
        // setembro: 2 de 3 no prazo (66,67%) -> abaixo da meta de 97%
        assertThat(body).contains("\"metaMensal\":97").contains("\"atingiuMeta\":false");
    }

    @Test
    void toleranciaDeAtrasoEDinamica() throws Exception {
        // PC-201 chegou 3 dias depois da previsão: atrasado com 1 dia de tolerância, no prazo com 5
        assertThat(get(BASE + "?dataInicio=2026-09-01&dataFim=2026-09-30"))
                .contains("\"toleranciaDias\":1").contains("\"entregasNoPrazo\":2");
        assertThat(get(BASE + "?dataInicio=2026-09-01&dataFim=2026-09-30&toleranciaDias=5"))
                .contains("\"toleranciaDias\":5").contains("\"entregasNoPrazo\":3")
                .contains("\"entregasAtrasadas\":0");
        assertThat(send(BASE + "?dataInicio=2026-09-01&dataFim=2026-09-30&toleranciaDias=31").statusCode())
                .isEqualTo(400);
    }

    @Test
    void periodoInvertidoDa400() throws Exception {
        assertThat(send(BASE + "?dataInicio=2026-09-30&dataFim=2026-09-01").statusCode()).isEqualTo(400);
    }

    // ---------- helpers ----------

    private ItemAnalisePedido item(String pedido, String fornecedor, String codigo, LocalDate previsao,
            LocalDate entrega) {
        return item(pedido, fornecedor, codigo, previsao, entrega, "MATERIA-PRIMA");
    }

    private ItemAnalisePedido item(String pedido, String fornecedor, String codigo, LocalDate previsao,
            LocalDate entrega, String rateio) {
        var emissao = previsao.minusDays(15);
        var base = new ItemAnalisePedido("RC-" + pedido, "Solicitante", emissao, null, pedido, "Comprador", emissao,
                previsao, entrega, fornecedor, "Brasil", codigo, "Descrição " + codigo, "UN", BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ZERO, "Almoxarifado", BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.TEN, "30 dias", null, null, null, null, rateio, null, null,
                null, null);
        var unidadeId = unidades.obterOuCriar("UN", "UN");
        return base
                .withUnidadeMedidaId(unidadeId)
                .withMaterialId(materiais.obterOuCriar(codigo, "Descrição " + codigo, unidadeId))
                .withFornecedorId(fornecedores.obterOuCriar(fornecedor, "Brasil"));
    }

    private String get(String path) throws Exception {
        var response = send(path);
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        return response.body();
    }

    private HttpResponse<String> send(String path) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path))
                .header("Authorization", "Bearer " + admin)
                .GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String login() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"admin\",\"password\":\"Admin12345\"}"))
                .build();
        var body = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
        Matcher m = TOKEN.matcher(body);
        assertThat(m.find()).as(body).isTrue();
        return m.group(1);
    }
}
