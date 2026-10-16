package br.lanx.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

/**
 * Autorização de ponta a ponta, pela rede de verdade: sem token (401), com
 * token
 * mas sem a permissão (403), com a permissão (200) e mudança de role valendo na
 * hora. Cobre a cadeia inteira: filtro JWT -> authorities -> @PreAuthorize.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.bootstrap.admin.username=admin",
        "app.bootstrap.admin.password=Admin12345",
        "app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@ActiveProfiles("test")
class AuthorizationIntegrationTest {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TOKEN = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"");

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void semTokenDa401() throws Exception {
        assertThat(send("GET", "/auth/roles", null, null).statusCode()).isEqualTo(401);
    }

    @Test
    void adminTemTodasAsPermissoes() throws Exception {
        String admin = login("admin", "Admin12345");

        assertThat(send("GET", "/auth/roles", admin, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/auth/features", admin, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/auth/users", admin, null).statusCode()).isEqualTo(200);
    }

    @Test
    void usuarioSemPermissaoDa403ComPermissaoDa200EMudancaDeRoleVaiNaHora() throws Exception {
        String admin = login("admin", "Admin12345");

        // role que só enxerga unidades de medida
        String roleId = id(send("POST", "/auth/roles", admin, """
                {"name":"SO_UNIDADES","description":"teste","permissions":["cadastros.unidade-medida:read"]}"""));
        send("POST", "/auth/users", admin, """
                {"username":"maria","password":"Senha12345","roleIds":["%s"]}""".formatted(roleId));
        String maria = login("maria", "Senha12345");

        assertThat(send("GET", "/unidade-medida", maria, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/auth/roles", maria, null).statusCode()).isEqualTo(403);
        assertThat(send("GET", "/auth/users", maria, null).statusCode()).isEqualTo(403);
        assertThat(send("GET", "/auth/me/navigation", maria, null).statusCode()).isEqualTo(200);

        // admin passa a conceder leitura de perfis: o MESMO token da maria já enxerga
        send("PUT", "/auth/roles/" + roleId, admin, """
                {"name":"SO_UNIDADES","description":"teste",
                 "permissions":["cadastros.unidade-medida:read","administracao.permissoes:read"]}""");
        assertThat(send("GET", "/auth/roles", maria, null).statusCode()).isEqualTo(200);
    }

    @Test
    void permissaoDesconhecidaNaRoleDa400() throws Exception {
        String admin = login("admin", "Admin12345");

        var response = send("POST", "/auth/roles", admin, """
                {"name":"INVENTADA","permissions":["modulo.que.nao.existe:read"]}""");
        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void analiseDePedidosExigePermissaoDeCompras() throws Exception {
        String admin = login("admin", "Admin12345");

        // o catálogo de compras chega na navegação do admin, com a ação extra "importar"
        String navigation = send("GET", "/auth/me/navigation", admin, null).body();
        assertThat(navigation).contains("\"compras.analise-pedidos\"").contains("\"importar\"");

        // role que só lê a análise, sem poder importar do ERP
        String roleId = id(send("POST", "/auth/roles", admin, """
                {"name":"SO_LEITURA_COMPRAS","description":"teste","permissions":["compras.analise-pedidos:read"]}"""));
        send("POST", "/auth/users", admin, """
                {"username":"joao","password":"Senha12345","roleIds":["%s"]}""".formatted(roleId));
        String joao = login("joao", "Senha12345");

        assertThat(send("POST", "/compras/relatorios/analise-pedidos/importar?dataInicio=2026-10-01&dataFim=2026-10-31",
                joao, null).statusCode()).isEqualTo(403);

        // sem nenhuma permissão de compras, nem a consulta é liberada
        String semCompras = id(send("POST", "/auth/roles", admin, """
                {"name":"SEM_COMPRAS","description":"teste","permissions":["cadastros.materiais:read"]}"""));
        send("POST", "/auth/users", admin, """
                {"username":"ana","password":"Senha12345","roleIds":["%s"]}""".formatted(semCompras));
        String ana = login("ana", "Senha12345");

        assertThat(send("GET", "/compras/relatorios/analise-pedidos", ana, null).statusCode()).isEqualTo(403);
    }

    // ---------- helpers ----------

    private String login(String username, String password) throws Exception {
        var response = send("POST", "/auth/login", null, """
                {"username":"%s","password":"%s"}""".formatted(username, password));
        assertThat(response.statusCode()).as("login de %s", username).isEqualTo(200);
        return find(TOKEN, response.body());
    }

    private String id(HttpResponse<String> response) {
        assertThat(response.statusCode()).as(response.body()).isEqualTo(201);
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
