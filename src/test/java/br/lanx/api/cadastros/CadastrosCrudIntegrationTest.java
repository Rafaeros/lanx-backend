package br.lanx.api.cadastros;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** CRUD completo de unidade de medida e materiais, pela rede, com o admin (todas as permissões). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345",
        "app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@ActiveProfiles("test")
class CadastrosCrudIntegrationTest {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void unidadeDeMedidaCrudCompleto() throws Exception {
        String t = adminToken();

        String id = id(send("POST", "/unidade-medida", t, """
                {"nome":"Metro","sigla":"m"}"""), 201);
        assertThat(send("POST", "/unidade-medida", t, """
                {"nome":"Outra","sigla":"m"}""").statusCode()).as("sigla duplicada").isEqualTo(409);

        assertThat(send("GET", "/unidade-medida/" + id, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/unidade-medida/" + java.util.UUID.randomUUID(), t, null).statusCode())
                .as("id inexistente").isEqualTo(404);

        assertThat(send("PUT", "/unidade-medida/" + id, t, """
                {"nome":"Metro linear","sigla":"m"}""").statusCode()).isEqualTo(200);
        assertThat(send("GET", "/unidade-medida/" + id, t, null).body()).contains("Metro linear");
        assertThat(send("PUT", "/unidade-medida/" + java.util.UUID.randomUUID(), t, """
                {"nome":"X","sigla":"x"}""").statusCode()).as("update inexistente").isEqualTo(404);

        assertThat(send("DELETE", "/unidade-medida/" + id, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/unidade-medida/" + id, t, null).statusCode()).isEqualTo(404);
        assertThat(send("DELETE", "/unidade-medida/" + id, t, null).statusCode()).as("delete repetido")
                .isEqualTo(404);
    }

    @Test
    void unidadeDeMedidaFindByNomeOrCreate() throws Exception {
        String t = adminToken();

        var first = send("POST", "/unidade-medida/find-or-create", t, """
                {"nome":"Litro","sigla":"L"}""");
        assertThat(first.statusCode()).as("criou").isEqualTo(201);

        var second = send("POST", "/unidade-medida/find-or-create", t, """
                {"nome":"litro","sigla":"LT"}""");
        assertThat(second.statusCode()).as("já existia (nome sem diferenciar maiúsculas)").isEqualTo(200);
        assertThat(find(ID, second.body())).isEqualTo(find(ID, first.body()));

        var conflito = send("POST", "/unidade-medida/find-or-create", t, """
                {"nome":"Outro litro","sigla":"L"}""");
        assertThat(conflito.statusCode()).as("nome novo com sigla de outra unidade").isEqualTo(409);
    }

    @Test
    void materiaisCrudCompletoEUnidadeEmUsoNaoSaiDoBanco() throws Exception {
        String t = adminToken();
        String unidade = id(send("POST", "/unidade-medida", t, """
                {"nome":"Peça","sigla":"pc"}"""), 201);

        var create = send("POST", "/materiais", t, """
                {"codigo":"MAT-1","descricao":"Parafuso","unidadeMedidaId":"%s"}""".formatted(unidade));
        String material = id(create, 201);
        assertThat(send("POST", "/materiais", t, """
                {"codigo":"MAT-1","descricao":"Outro","unidadeMedidaId":"%s"}""".formatted(unidade)).statusCode())
                .as("código duplicado").isEqualTo(409);
        assertThat(send("POST", "/materiais", t, """
                {"codigo":"MAT-2","descricao":"X","unidadeMedidaId":"%s"}"""
                .formatted(java.util.UUID.randomUUID())).statusCode()).as("unidade inexistente").isEqualTo(404);
        assertThat(send("POST", "/materiais", t, "{}").statusCode()).as("campos obrigatórios").isEqualTo(400);

        assertThat(send("GET", "/materiais/" + material, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/materiais?codigo=mat-1", t, null).body()).contains("Parafuso");
        assertThat(send("GET", "/materiais", t, null).statusCode()).as("sem filtros").isEqualTo(200);

        assertThat(send("PUT", "/materiais/" + material, t, """
                {"codigo":"MAT-1","descricao":"Parafuso sextavado","unidadeMedidaId":"%s"}"""
                .formatted(unidade)).body()).contains("Parafuso sextavado");

        var again = send("POST", "/materiais/find-or-create", t, """
                {"codigo":"MAT-1","descricao":"ignorado","unidadeMedidaId":"%s"}""".formatted(unidade));
        assertThat(again.statusCode()).as("já existia").isEqualTo(200);
        assertThat(again.body()).contains("Parafuso sextavado");
        assertThat(send("POST", "/materiais/find-or-create", t, """
                {"codigo":"MAT-3","descricao":"Porca","unidadeMedidaId":"%s"}""".formatted(unidade)).statusCode())
                .as("criou").isEqualTo(201);

        // unidade usada por material não pode ser excluída
        assertThat(send("DELETE", "/unidade-medida/" + unidade, t, null).statusCode()).isEqualTo(409);

        assertThat(send("DELETE", "/materiais/" + material, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/materiais/" + material, t, null).statusCode()).isEqualTo(404);
        assertThat(send("DELETE", "/materiais/" + material, t, null).statusCode()).isEqualTo(404);
    }

    // ---------- helpers ----------

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
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
