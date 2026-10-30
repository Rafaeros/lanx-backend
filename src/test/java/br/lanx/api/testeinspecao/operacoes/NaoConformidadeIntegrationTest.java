package br.lanx.api.testeinspecao.operacoes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Tipos de não conformidade e não conformidades com anexo (multipart), pela rede, com o admin. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345",
        "app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "app.storage.base-dir=target/test-anexos"
})
@ActiveProfiles("test")
class NaoConformidadeIntegrationTest {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
    private static final String BASE = "/teste-inspecao/operacoes/nao-conformidade";

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void tiposDeNaoConformidadeCrudCompleto() throws Exception {
        String t = adminToken();

        String id = id(send("POST", "/tipos-nao-conformidade", t, """
                {"nome":"Inversão"}"""), 201);
        assertThat(send("POST", "/tipos-nao-conformidade", t, """
                {"nome":"inversão"}""").statusCode()).as("nome duplicado").isEqualTo(409);
        assertThat(send("PUT", "/tipos-nao-conformidade/" + id, t, """
                {"nome":"Inversão de fios"}""").statusCode()).isEqualTo(200);
        assertThat(send("GET", "/tipos-nao-conformidade?busca=fios", t, null).body()).contains("Inversão de fios");

        assertThat(send("DELETE", "/tipos-nao-conformidade/" + id, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/tipos-nao-conformidade/" + id, t, null).statusCode()).isEqualTo(404);
        assertThat(send("POST", "/tipos-nao-conformidade", t, """
                {"nome":"Inversão de fios"}""").statusCode()).as("nome de excluído fica livre").isEqualTo(201);
    }

    @Test
    void naoConformidadeComAnexoEdicaoExportacaoEExclusao() throws Exception {
        String t = adminToken();
        String tipo = id(send("POST", "/tipos-nao-conformidade", t, """
                {"nome":"Termo s/ retrair"}"""), 201);
        String departamento = id(send("POST", "/departamentos", t, """
                {"nome":"Montagem NC"}"""), 201);
        String op = criarOp(t);

        String json = """
                {"tipoNaoConformidadeId":"%s","departamentoId":"%s","ordemDeProducaoId":"%s",
                 "quantidadeNaoConforme":6,"dataNaoConformidade":"2025-01-04"}""".formatted(tipo, departamento, op);

        assertThat(multipart(t, json, "application/pdf", "laudo.pdf", new byte[0]).statusCode())
                .as("anexo vazio").isEqualTo(400);
        assertThat(multipart(t, json, "text/plain", "notas.txt", "x".getBytes()).statusCode())
                .as("tipo de arquivo não permitido").isEqualTo(400);
        assertThat(multipart(t, json.replace("\"quantidadeNaoConforme\":6", "\"quantidadeNaoConforme\":0"),
                "application/pdf", "laudo.pdf", "%PDF".getBytes()).statusCode()).as("quantidade zero").isEqualTo(400);

        var criada = multipart(t, json, "application/pdf", "C:\\fakepath\\laudo.pdf", "%PDF-1.4".getBytes());
        String nc = id(criada, 201);
        assertThat(criada.body()).contains("\"nomeArquivo\":\"laudo.pdf\"").contains("\"nome\":\"Montagem NC\"");

        var detalhe = send("GET", BASE + "/" + nc, t, null);
        assertThat(detalhe.body()).contains("\"codigo\":\"OP-NC01\"").contains("\"clienteNome\":\"Cliente NC\"");
        String anexo = find(Pattern.compile("\"anexos\"\\s*:\\s*\\[\\s*\\{\"id\"\\s*:\\s*\"([^\"]+)\""), detalhe.body());
        var download = sendBytes(BASE + "/" + nc + "/anexos/" + anexo, t);
        assertThat(download.statusCode()).isEqualTo(200);
        assertThat(new String(download.body(), StandardCharsets.UTF_8)).isEqualTo("%PDF-1.4");

        assertThat(send("GET", BASE + "?busca=montagem&dataInicio=2025-01-01&dataFim=2025-01-31", t, null).body())
                .contains(nc);
        assertThat(send("GET", BASE + "?dataInicio=2025-02-01", t, null).body()).as("fora do período")
                .doesNotContain(nc);

        var editada = send("PUT", BASE + "/" + nc, t, json.replace("\"quantidadeNaoConforme\":6",
                "\"quantidadeNaoConforme\":10"));
        assertThat(editada.statusCode()).as(editada.body()).isEqualTo(200);
        assertThat(editada.body()).contains("\"quantidadeNaoConforme\":10");

        var indicadores = send("GET", BASE + "/indicadores?dataInicio=2025-01-01&dataFim=2025-02-28", t, null);
        assertThat(indicadores.statusCode()).as(indicadores.body()).isEqualTo(200);
        assertThat(indicadores.body()).contains("\"nome\":\"Montagem NC\"").contains("\"mes\":\"2025-02\"")
                .contains("\"percentualNaoConforme\":1.25");

        var planilha = sendBytes(BASE + "/exportar?dataInicio=2025-01-01&dataFim=2025-01-31", t);
        assertThat(planilha.statusCode()).isEqualTo(200);
        assertThat(planilha.headers().firstValue("Content-Type")).hasValueSatisfying(
                ct -> assertThat(ct).contains("spreadsheetml"));
        assertThat(planilha.body()).startsWith((byte) 'P', (byte) 'K');

        assertThat(send("DELETE", "/tipos-nao-conformidade/" + tipo, t, null).statusCode()).as("tipo em uso")
                .isEqualTo(409);
        assertThat(send("DELETE", "/departamentos/" + departamento, t, null).statusCode()).as("departamento em uso")
                .isEqualTo(409);

        assertThat(send("DELETE", BASE + "/" + nc, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", BASE + "/" + nc, t, null).statusCode()).isEqualTo(404);
        assertThat(send("DELETE", "/tipos-nao-conformidade/" + tipo, t, null).statusCode()).as("sem NCs, sai")
                .isEqualTo(200);
    }

    // ---------- helpers ----------

    private String criarOp(String t) throws Exception {
        String unidade = id(send("POST", "/unidade-medida", t, """
                {"nome":"Peça NC","sigla":"pnc"}"""), 201);
        String material = id(send("POST", "/materiais", t, """
                {"codigo":"WTR215-NC","descricao":"Chicote NC","unidadeMedidaId":"%s","tipo":"PRODUTO_ACABADO"}"""
                .formatted(unidade)), 201);
        String cliente = id(send("POST", "/clientes", t, """
                {"nome":"Cliente NC"}"""), 201);
        return id(send("POST", "/producao/operacoes/ordens-de-producao", t, """
                {"codigo":"OP-NC01","clienteId":"%s","materialId":"%s","quantidade":800,"previsao":"2025-01-24"}"""
                .formatted(cliente, material)), 201);
    }

    private String adminToken() throws Exception {
        var response = send("POST", "/auth/login", null, """
                {"username":"admin","password":"Admin12345"}""");
        assertThat(response.statusCode()).isEqualTo(200);
        return find(TOKEN, response.body());
    }

    private String id(HttpResponse<String> response, int expectedStatus) {
        assertThat(response.statusCode()).as(response.body()).isEqualTo(expectedStatus);
        return find(ID, response.body());
    }

    private static String find(Pattern pattern, String body) {
        Matcher m = pattern.matcher(body);
        assertThat(m.find()).as(body).isTrue();
        return m.group(1);
    }

    private HttpResponse<String> send(String method, String path, String token, String body) throws Exception {
        var builder = request(path, token).header("Content-Type", "application/json");
        builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<byte[]> sendBytes(String path, String token) throws Exception {
        return http.send(request(path, token).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    /** POST multipart com a parte "request" (JSON) e um arquivo em "files". */
    private HttpResponse<String> multipart(String token, String json, String contentType, String filename,
            byte[] conteudo) throws Exception {
        String boundary = "----lanx" + UUID.randomUUID();
        var body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"request\"\r\n"
                + "Content-Type: application/json\r\n\r\n" + json + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"files\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(conteudo);
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        var builder = request(BASE, token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path, String token) {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }
}
