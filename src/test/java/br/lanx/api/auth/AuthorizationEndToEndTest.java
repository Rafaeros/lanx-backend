package br.lanx.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Sobe a aplicação inteira (H2) e conversa por HTTP de verdade: admin criado pelo
 * bootstrap, login, token, interceptor de permissões e banco.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345"
})
@ActiveProfiles("test")
class AuthorizationEndToEndTest {

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> call(String method, String path, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1" + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String username, String password) throws Exception {
        var res = call("POST", "/auth/login", null,
                "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password));
        assertThat(res.statusCode()).as("login " + username + ": " + res.body()).isEqualTo(200);
        return extract(res.body(), "\"accessToken\":\"([^\"]+)\"");
    }

    private static String extract(String body, String regex) {
        Matcher m = Pattern.compile(regex).matcher(body);
        assertThat(m.find()).as("padrão " + regex + " em " + body).isTrue();
        return m.group(1);
    }

    @Test
    @DisplayName("admin cria role e usuário; o usuário só alcança o que a role permite")
    void fluxoCompleto() throws Exception {
        // sem token → 401
        assertThat(call("GET", "/auth/features", null, null).statusCode()).isEqualTo(401);

        // admin vê o catálogo
        String admin = login("admin", "Admin12345");
        var catalog = call("GET", "/auth/features", admin, null);
        assertThat(catalog.statusCode()).isEqualTo(200);
        assertThat(catalog.body()).contains("administracao.usuarios", "administracao.permissoes", "READ");

        // admin cria uma role que só lê usuários
        var role = call("POST", "/auth/roles", admin, """
                {"name":"visitante","permissions":{"crud":{"administracao.usuarios":["read"]}}}""");
        assertThat(role.statusCode()).as(role.body()).isEqualTo(201);
        String roleId = extract(role.body(), "\"id\":\"([0-9a-f-]{36})\"");

        // role repetida → 409; funcionalidade inexistente → 400
        assertThat(call("POST", "/auth/roles", admin, """
                {"name":"VISITANTE","permissions":{"crud":{"administracao.usuarios":["READ"]}}}""")
                .statusCode()).isEqualTo(409);
        assertThat(call("POST", "/auth/roles", admin, """
                {"name":"OUTRA","permissions":{"crud":{"nao.existe":["READ"]}}}""")
                .statusCode()).isEqualTo(400);

        // admin cria o usuário com essa role
        var user = call("POST", "/auth/users", admin, """
                {"username":"visitante","password":"senha1234","roleIds":["%s"]}""".formatted(roleId));
        assertThat(user.statusCode()).as(user.body()).isEqualTo(201);
        String userId = extract(user.body(), "\"id\":\"([0-9a-f-]{36})\"");

        // o visitante entra e o token não carrega permissões, só identidade
        String token = login("visitante", "senha1234");
        assertThat(token.split("\\.")).hasSize(3);

        assertThat(call("GET", "/auth/users/" + userId, token, null).statusCode()).isEqualTo(200); // READ ok
        assertThat(call("POST", "/auth/users", token, """
                {"username":"x1234","password":"senha1234","roleIds":["%s"]}""".formatted(roleId))
                .statusCode()).isEqualTo(403); // CREATE negado
        assertThat(call("GET", "/auth/features", token, null).statusCode()).isEqualTo(403); // outra funcionalidade
        assertThat(call("GET", "/auth/roles", token, null).statusCode()).isEqualTo(403);

        // a navegação do visitante mostra só o ramo que ele alcança
        var nav = call("GET", "/auth/me/navigation", token, null);
        assertThat(nav.statusCode()).isEqualTo(200);
        assertThat(nav.body()).contains("administracao.usuarios").doesNotContain("administracao.permissoes");

        // admin dá a ele, por exceção individual, permissão de criar usuários: vale na hora, sem novo login
        var edit = call("PUT", "/auth/users/" + userId, admin, """
                {"roleIds":["%s"],"grants":{"crud":{"administracao.usuarios":["CREATE"]}},"active":true}"""
                .formatted(roleId));
        assertThat(edit.statusCode()).as(edit.body()).isEqualTo(200);
        assertThat(call("POST", "/auth/users", token, """
                {"username":"outro123","password":"senha1234","roleIds":["%s"]}""".formatted(roleId))
                .statusCode()).isEqualTo(201);

        // admin desativa: o token que ele já tinha deixa de valer para tudo
        call("PUT", "/auth/users/" + userId, admin, """
                {"roleIds":["%s"],"active":false}""".formatted(roleId));
        assertThat(call("GET", "/auth/users/" + userId, token, null).statusCode()).isEqualTo(403);
    }
}
