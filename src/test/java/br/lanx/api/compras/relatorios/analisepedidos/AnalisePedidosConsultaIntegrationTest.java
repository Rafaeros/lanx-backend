package br.lanx.api.compras.relatorios.analisepedidos;

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
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeCompradorPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeFornecedorPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeRateioPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeMaterialPort;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeUnidadeMedidaPort;
import br.lanx.api.compras.relatorios.analisepedidos.domain.ItemAnalisePedido;

/** Consulta paginada, filtros e resumo da análise de pedidos, pela API de verdade. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345",
        "app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@ActiveProfiles("test")
class AnalisePedidosConsultaIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
    private static final String BASE = "/compras/relatorios/analise-pedidos";
    private static final String OUTUBRO = "dataInicio=2026-10-01&dataFim=2026-10-31";

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

    @Autowired
    NormalizeCompradorPort compradores;

    @Autowired
    NormalizeRateioPort rateios;

    private final HttpClient http = HttpClient.newHttpClient();
    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        persistence.salvar(List.of(
                item("PC-100", "Aço Tubos SA", "AP-MAT-1", LocalDate.of(2026, 10, 2), null, "10", "0", "10", "100"),
                item("PC-101", "Valvtech Ltda", "AP-MAT-2", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 15), "10", "4", "6", "200"),
                item("PC-102", "Valvtech Ltda", "AP-MAT-3", LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 29), "5", "5", "0", "300"),
                // fora do período
                item("PC-090", "Aço Tubos SA", "AP-MAT-1", LocalDate.of(2026, 9, 20), null, "1", "0", "1", "999")));
        admin = login();
    }

    @Test
    void devolvePaginadoEOrdenadoPorEmissao() throws Exception {
        var body = get(BASE + "?" + OUTUBRO + "&page=0&size=2");
        assertThat(body).contains("\"totalElements\":3").contains("\"totalPages\":2").contains("\"size\":2");
        assertThat(body.indexOf("PC-100")).isLessThan(body.indexOf("PC-101"));
        assertThat(body).doesNotContain("PC-102").doesNotContain("PC-090");
        assertThat(body).contains("\"fornecedor\":{").contains("\"nome\":\"Aço Tubos SA\"")
                .contains("\"pais\":\"Brasil\"").contains("\"codigo\":\"AP-MAT-1\"").contains("\"sigla\":\"UN\"");

        assertThat(get(BASE + "?" + OUTUBRO + "&page=1&size=2")).contains("PC-102");
    }

    @Test
    void filtraPorBuscaESituacao() throws Exception {
        assertThat(get(BASE + "?" + OUTUBRO + "&busca=valvtech")).contains("\"totalElements\":2");
        assertThat(get(BASE + "?" + OUTUBRO + "&busca=ap-mat-3")).contains("\"totalElements\":1").contains("PC-102");

        var parciais = get(BASE + "?" + OUTUBRO + "&situacao=PARCIAL");
        assertThat(parciais).contains("\"totalElements\":1").contains("PC-101")
                .contains("\"situacao\":\"PARCIAL\"").contains("\"atendimentoParcial\":true")
                .contains("\"leadTimeDias\":10");

        assertThat(get(BASE + "?" + OUTUBRO + "&situacao=PENDENTE")).contains("\"totalElements\":1").contains("PC-100");
        assertThat(get(BASE + "?" + OUTUBRO + "&situacao=ATENDIDO")).contains("\"totalElements\":1").contains("PC-102");
    }

    @Test
    void resumoConsideraTodosOsItensFiltrados() throws Exception {
        var resumo = get(BASE + "/resumo?" + OUTUBRO);
        assertThat(resumo).contains("\"totalItens\":3").contains("\"pendentes\":1").contains("\"parciais\":1")
                .contains("\"atendidos\":1").contains("\"leadTimeMedioDias\":16");
        assertThat(resumo).containsPattern("\"valorTotal\":600(\\.0+)?[,}]");
    }

    @Test
    void filtraPeloIdDoRateioEListaOsRateiosDosItens() throws Exception {
        var rateio = rateios.obterOuCriar("AP-RATEIO");
        persistence.salvar(List.of(item("PC-103", "Valvtech Ltda", "AP-MAT-4", LocalDate.of(2026, 10, 9), null,
                "1", "0", "1", "50").withRateioId(rateio)));

        var filtrado = get(BASE + "?" + OUTUBRO + "&rateios=" + rateio);
        assertThat(filtrado).contains("\"totalElements\":1").contains("PC-103")
                .contains("\"rateio\":{\"id\":\"" + rateio + "\",\"nome\":\"AP-RATEIO\"}");
        assertThat(get(BASE + "/resumo?" + OUTUBRO + "&rateios=" + rateio)).contains("\"totalItens\":1");
        assertThat(get(BASE + "?" + OUTUBRO)).as("sem filtro, todos").contains("\"totalElements\":4");
        assertThat(get(BASE + "/rateios")).contains("{\"id\":\"" + rateio + "\",\"nome\":\"AP-RATEIO\"}");
    }

    @Test
    void periodoInvertidoOuSituacaoInvalidaDa400() throws Exception {
        assertThat(send(BASE + "?dataInicio=2026-10-31&dataFim=2026-10-01").statusCode()).isEqualTo(400);
        assertThat(send(BASE + "?" + OUTUBRO + "&situacao=QUALQUER").statusCode()).isEqualTo(400);
    }

    // ---------- helpers ----------

    private ItemAnalisePedido item(String pedido, String fornecedor, String codigo, LocalDate emissao,
            LocalDate entrega, String solicitada, String atendida, String saldo, String valor) {
        var base = new ItemAnalisePedido("RC-" + pedido, "Solicitante", emissao, null, pedido, "Comprador", emissao,
                null, entrega, fornecedor, "Brasil", codigo, "Descrição " + codigo, "UN", new BigDecimal(solicitada),
                new BigDecimal(atendida), new BigDecimal(saldo), "Almoxarifado", BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal(valor), "30 dias", null, null, null, null, null, null, null,
                null, null);
        var unidadeId = unidades.obterOuCriar("UN", "UN");
        return base
                .withUnidadeMedidaId(unidadeId)
                .withMaterialId(materiais.obterOuCriar(codigo, "Descrição " + codigo, unidadeId))
                .withFornecedorId(fornecedores.obterOuCriar(fornecedor, "Brasil"))
                .withCompradorId(compradores.obterOuCriar(base.comprador()))
                .withRateioId(rateios.obterOuCriar(base.rateio()));
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
