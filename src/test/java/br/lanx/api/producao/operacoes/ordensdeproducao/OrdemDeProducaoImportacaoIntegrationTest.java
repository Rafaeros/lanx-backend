package br.lanx.api.producao.operacoes.ordensdeproducao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** POST /importar pela rede, com o admin: cria, atualiza e cadastra cliente/produto que faltam. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345",
        "app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@ActiveProfiles("test")
class OrdemDeProducaoImportacaoIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
    private static final String BASE = "/producao/operacoes/ordens-de-producao";

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void importaCriaAtualizaEIgnoraFinalizadas() throws Exception {
        String t = adminToken();
        LocalDate entrega = LocalDate.of(2026, 12, 15);

        var primeira = importar(t, false, planilha(
                new Object[] { entrega, "OP-9900001", "CLIENTE IMPORTADO (MATRIZ)", "IMP001 000 000", "Chicote importado", 10, "Liberada" },
                new Object[] { entrega, "OP-9900002", "CLIENTE IMPORTADO (MATRIZ)", "IMP001 000 000", "Chicote importado", 5, "Iniciada" },
                new Object[] { entrega, "OP-9900003", "CLIENTE IMPORTADO (MATRIZ)", "IMP001 000 000", "Chicote importado", 7, "Finalizada" },
                new Object[] { entrega, "sem código", "CLIENTE IMPORTADO (MATRIZ)", "IMP001 000 000", "Chicote importado", 1, "Liberada" }));
        assertThat(primeira.statusCode()).as(primeira.body()).isEqualTo(200);
        assertThat(primeira.body())
                .contains("\"linhasLidas\":4", "\"criadas\":2", "\"atualizadas\":0", "\"finalizadasIgnoradas\":1",
                        "\"clientesCriados\":1", "\"materiaisCriados\":1", "\"linha\":5");

        var segunda = importar(t, true, planilha(
                new Object[] { entrega, "OP-9900001", "cliente importado (matriz)", "IMP001 000 000", "Chicote importado", 12, "Iniciada" },
                new Object[] { entrega, "OP-9900003", "CLIENTE IMPORTADO (MATRIZ)", "IMP001 000 000", "Chicote importado", 7, "Finalizada" }));
        assertThat(segunda.body())
                .contains("\"criadas\":1", "\"atualizadas\":1", "\"clientesCriados\":0", "\"materiaisCriados\":0");

        var lista = send("GET", BASE + "?busca=OP-99000", t);
        assertThat(lista.body()).contains("OP-9900001", "OP-9900002", "OP-9900003", "\"quantidade\":12");
    }

    @Test
    void arquivoQueNaoEPlanilhaVolta400() throws Exception {
        var resposta = importar(adminToken(), false, "não é planilha".getBytes(StandardCharsets.UTF_8));
        assertThat(resposta.statusCode()).as(resposta.body()).isEqualTo(400);
    }

    // ---------- helpers ----------

    private static final List<String> CABECALHO = List.of("Entrega", "Código", "Cliente", "Cód. Material", "Material",
            "Quantidade", "Status");

    private static byte[] planilha(Object[]... linhas) throws Exception {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var aba = wb.createSheet();
            Row cab = aba.createRow(0);
            for (int i = 0; i < CABECALHO.size(); i++) {
                cab.createCell(i).setCellValue(CABECALHO.get(i));
            }
            for (int r = 0; r < linhas.length; r++) {
                Row row = aba.createRow(r + 1);
                Object[] l = linhas[r];
                row.createCell(0).setCellValue(DateUtil.getExcelDate((LocalDate) l[0]));
                for (int i = 1; i < l.length; i++) {
                    if (l[i] instanceof Number n) {
                        row.createCell(i).setCellValue(n.doubleValue());
                    } else {
                        row.createCell(i).setCellValue((String) l[i]);
                    }
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private HttpResponse<String> importar(String token, boolean incluirFinalizadas, byte[] arquivo) throws Exception {
        String boundary = "----lanx" + UUID.randomUUID();
        var body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"arquivo\"; filename=\"ordens.xlsx\"\r\n"
                + "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        body.write(arquivo);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + BASE
                + "/importar?incluirFinalizadas=" + incluirFinalizadas))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String adminToken() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"username":"admin","password":"Admin12345"}"""))
                .build();
        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        Matcher m = TOKEN.matcher(response.body());
        assertThat(m.find()).as(response.body()).isTrue();
        return m.group(1);
    }

    private HttpResponse<String> send(String method, String path, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path))
                .header("Authorization", "Bearer " + token)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
