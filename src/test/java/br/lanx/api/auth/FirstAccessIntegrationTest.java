package br.lanx.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Primeiro acesso: usuário cadastrado pelo admin entra com a senha provisória,
 * não recebe token e precisa definir uma senha forte em /auth/first-access
 * (rota pública).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345",
        "app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@ActiveProfiles("test")
class FirstAccessIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final String NOVA = "Compras#2026x";

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();
    private String username;

    @BeforeEach
    void cadastraUsuario() throws Exception {
        String admin = token(send("POST", "/auth/login", null, login("admin", "Admin12345")));
        String roleId = find(ID, send("GET", "/auth/roles", admin, null).body());
        // username único: o contexto (e o banco H2) é compartilhado entre as classes de teste
        username = "novo" + System.nanoTime() % 1_000_000;
        var created = send("POST", "/auth/users", admin, """
                {"username":"%s","password":"Provisoria1","roleIds":["%s"]}""".formatted(username, roleId));
        assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
    }

    @Test
    void loginComSenhaProvisoriaNaoDevolveToken() throws Exception {
        var response = send("POST", "/auth/login", null, login(username, "Provisoria1"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"firstAccess\":true").doesNotContain("accessToken");
    }

    @Test
    void definirSenhaForteEntraNoSistemaEOLoginSeguinteENormal() throws Exception {
        var response = send("POST", "/auth/first-access", null, troca("Provisoria1", NOVA, NOVA));
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        String token = token(response);
        assertThat(send("GET", "/auth/me/navigation", token, null).statusCode()).isEqualTo(200);

        // depois: login normal com a nova senha; a provisória não vale mais
        var login = send("POST", "/auth/login", null, login(username, NOVA));
        assertThat(login.body()).contains("accessToken").contains("\"firstAccess\":false");
        assertThat(send("POST", "/auth/login", null, login(username, "Provisoria1")).statusCode()).isEqualTo(401);

        // e o primeiro acesso não pode ser refeito
        assertThat(send("POST", "/auth/first-access", null, troca(NOVA, "Outra#Senha2026", "Outra#Senha2026"))
                .statusCode()).isEqualTo(409);
    }

    @Test
    void senhaFracaOuConfirmacaoDiferenteDa400() throws Exception {
        assertThat(send("POST", "/auth/first-access", null, troca("Provisoria1", "fraca", "fraca"))
                .statusCode()).isEqualTo(400);
        assertThat(send("POST", "/auth/first-access", null, troca("Provisoria1", "SemSimbolo2026", "SemSimbolo2026"))
                .statusCode()).isEqualTo(400);
        var diferente = send("POST", "/auth/first-access", null, troca("Provisoria1", NOVA, NOVA + "!"));
        assertThat(diferente.statusCode()).isEqualTo(400);
        assertThat(diferente.body()).contains("confirmação");
    }

    @Test
    void senhaProvisoriaErradaDa401() throws Exception {
        assertThat(send("POST", "/auth/first-access", null, troca("Errada123", NOVA, NOVA)).statusCode())
                .isEqualTo(401);
    }

    // ---------- helpers ----------

    private static String login(String user, String password) {
        return """
                {"username":"%s","password":"%s"}""".formatted(user, password);
    }

    private String troca(String atual, String nova, String confirmacao) {
        return """
                {"username":"%s","currentPassword":"%s","newPassword":"%s","confirmPassword":"%s"}"""
                .formatted(username, atual, nova, confirmacao);
    }

    private static String token(HttpResponse<String> response) {
        return find(TOKEN, response.body());
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
