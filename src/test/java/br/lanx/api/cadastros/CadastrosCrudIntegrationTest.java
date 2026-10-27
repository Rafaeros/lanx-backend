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

    @Test
    void departamentosCrudCompleto() throws Exception {
        String t = adminToken();

        String id = id(send("POST", "/departamentos", t, """
                {"nome":"Extrusão"}"""), 201);
        assertThat(send("POST", "/departamentos", t, """
                {"nome":"extrusão"}""").statusCode()).as("nome duplicado").isEqualTo(409);

        assertThat(send("PUT", "/departamentos/" + id, t, """
                {"nome":"Extrusão"}""").statusCode()).as("salvar sem trocar o nome").isEqualTo(200);
        assertThat(send("GET", "/departamentos?busca=extr", t, null).body()).contains("Extrusão");

        assertThat(send("DELETE", "/departamentos/" + id, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/departamentos/" + id, t, null).statusCode()).isEqualTo(404);
    }

    @Test
    void operadoresCrudCompletoComDepartamentoOpcional() throws Exception {
        String t = adminToken();
        String departamento = id(send("POST", "/departamentos", t, """
                {"nome":"Trefilação"}"""), 201);

        var create = send("POST", "/operadores", t, """
                {"nome":"João Silva","departamentoId":"%s"}""".formatted(departamento));
        String operador = id(create, 201);
        assertThat(create.body()).contains("Trefilação");
        assertThat(send("POST", "/operadores", t, """
                {"nome":"joão silva"}""").statusCode()).as("nome duplicado").isEqualTo(409);
        assertThat(send("POST", "/operadores", t, """
                {"nome":"Maria","departamentoId":"%s"}""".formatted(java.util.UUID.randomUUID())).statusCode())
                .as("departamento inexistente").isEqualTo(404);

        assertThat(send("GET", "/operadores?busca=joão", t, null).body()).contains(departamento);
        // departamento com operador não pode ser excluído
        assertThat(send("DELETE", "/departamentos/" + departamento, t, null).statusCode()).isEqualTo(409);

        var semDepartamento = send("PUT", "/operadores/" + operador, t, """
                {"nome":"João Silva","departamentoId":null}""");
        assertThat(semDepartamento.statusCode()).isEqualTo(200);
        assertThat(semDepartamento.body()).as("departamento removido").doesNotContain("Trefilação");
        assertThat(send("PUT", "/operadores/" + operador, t, """
                {"nome":" "}""").statusCode()).as("nome em branco").isEqualTo(400);

        assertThat(send("DELETE", "/operadores/" + operador, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/operadores/" + operador, t, null).statusCode()).isEqualTo(404);
        assertThat(send("DELETE", "/departamentos/" + departamento, t, null).statusCode())
                .as("sem operadores, sai do cadastro").isEqualTo(200);
    }

    @Test
    void rateiosCrudCompleto() throws Exception {
        String t = adminToken();

        String id = id(send("POST", "/rateios", t, """
                {"nome":"RATEIO-CRUD"}"""), 201);
        assertThat(send("POST", "/rateios", t, """
                {"nome":"rateio-crud"}""").statusCode()).as("nome duplicado").isEqualTo(409);
        assertThat(send("PUT", "/rateios/" + id, t, """
                {"nome":"RATEIO-CRUD"}""").statusCode()).as("salvar sem trocar o nome").isEqualTo(200);
        assertThat(send("GET", "/rateios?busca=crud", t, null).body()).contains("RATEIO-CRUD");

        assertThat(send("DELETE", "/rateios/" + id, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/rateios/" + id, t, null).statusCode()).isEqualTo(404);
        assertThat(send("POST", "/rateios", t, """
                {"nome":"RATEIO-CRUD"}""").statusCode()).as("nome de rateio excluído pode voltar").isEqualTo(201);
    }

    @Test
    void compradoresCrudComVinculoOpcionalAoUsuario() throws Exception {
        String t = adminToken();
        String adminId = find(Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"username\"\\s*:\\s*\"admin\""),
                send("GET", "/auth/users", t, null).body());

        var semUsuario = send("POST", "/compradores", t, """
                {"nome":"MARIANA SILVA"}""");
        String id = id(semUsuario, 201);
        assertThat(semUsuario.body()).as("comprador do ERP sem login").doesNotContain("\"usuario\"");
        assertThat(send("POST", "/compradores", t, """
                {"nome":"mariana silva"}""").statusCode()).as("nome duplicado").isEqualTo(409);
        // listagem só com compradores sem usuário (caso dos que vêm do ERP)
        var listaSemVinculo = send("GET", "/compradores?busca=mariana", t, null);
        assertThat(listaSemVinculo.statusCode()).as(listaSemVinculo.body()).isEqualTo(200);
        assertThat(listaSemVinculo.body()).contains("MARIANA SILVA");

        var vinculado = send("PUT", "/compradores/" + id, t, """
                {"nome":"MARIANA SILVA","userId":"%s"}""".formatted(adminId));
        assertThat(vinculado.statusCode()).isEqualTo(200);
        assertThat(vinculado.body()).contains("\"username\":\"admin\"");
        assertThat(send("GET", "/compradores?busca=mariana", t, null).body()).contains("\"username\":\"admin\"");
        assertThat(send("PUT", "/compradores/" + id, t, """
                {"nome":"MARIANA SILVA","userId":"%s"}""".formatted(java.util.UUID.randomUUID())).statusCode())
                .as("usuário inexistente").isEqualTo(404);

        var desvinculado = send("PUT", "/compradores/" + id, t, """
                {"nome":"MARIANA SILVA","userId":null}""");
        assertThat(desvinculado.body()).doesNotContain("\"usuario\"");

        assertThat(send("DELETE", "/compradores/" + id, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/compradores/" + id, t, null).statusCode()).isEqualTo(404);
    }

    @Test
    void operadorVinculadoAUmUsuarioNoMaximo() throws Exception {
        String t = adminToken();
        String adminId = find(Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"username\"\\s*:\\s*\"admin\""),
                send("GET", "/auth/users", t, null).body());

        var vinculado = send("POST", "/operadores", t, """
                {"nome":"Operador Vinculado","userId":"%s"}""".formatted(adminId));
        String operador = id(vinculado, 201);
        assertThat(vinculado.body()).contains("\"username\":\"admin\"");
        assertThat(send("GET", "/operadores?busca=vinculado", t, null).body()).contains("\"username\":\"admin\"");

        assertThat(send("POST", "/operadores", t, """
                {"nome":"Outro Operador","userId":"%s"}""".formatted(adminId)).statusCode())
                .as("usuário já é de outro operador").isEqualTo(409);
        assertThat(send("POST", "/operadores", t, """
                {"nome":"Operador Fantasma","userId":"%s"}""".formatted(java.util.UUID.randomUUID())).statusCode())
                .as("usuário inexistente").isEqualTo(404);
        assertThat(send("PUT", "/operadores/" + operador, t, """
                {"nome":"Operador Vinculado","userId":"%s"}""".formatted(adminId)).statusCode())
                .as("salvar o próprio vínculo de novo").isEqualTo(200);

        // excluído, o usuário fica livre para outro operador (índice parcial + @SQLRestriction)
        assertThat(send("DELETE", "/operadores/" + operador, t, null).statusCode()).isEqualTo(200);
        id(send("POST", "/operadores", t, """
                {"nome":"Operador Novo","userId":"%s"}""".formatted(adminId)), 201);
    }

    @Test
    void materialNasceComoMateriaPrimaEFiltraPorTipo() throws Exception {
        String t = adminToken();
        String unidade = id(send("POST", "/unidade-medida", t, """
                {"nome":"Peça tipo","sigla":"pct"}"""), 201);

        var materia = send("POST", "/materiais", t, """
                {"codigo":"TIPO-MP","descricao":"Cobre","unidadeMedidaId":"%s"}""".formatted(unidade));
        id(materia, 201);
        assertThat(materia.body()).as("sem tipo = matéria-prima").contains("\"tipo\":\"MATERIA_PRIMA\"");

        String produto = id(send("POST", "/materiais", t, """
                {"codigo":"TIPO-PA","descricao":"Chicote","unidadeMedidaId":"%s","tipo":"PRODUTO_ACABADO"}"""
                .formatted(unidade)), 201);
        var semTipo = send("PUT", "/materiais/" + produto, t, """
                {"codigo":"TIPO-PA","descricao":"Chicote H5","unidadeMedidaId":"%s"}""".formatted(unidade));
        assertThat(semTipo.body()).as("update sem tipo mantém o atual").contains("\"tipo\":\"PRODUTO_ACABADO\"");

        var produtos = send("GET", "/materiais?tipo=PRODUTO_ACABADO", t, null).body();
        assertThat(produtos).contains("TIPO-PA").doesNotContain("TIPO-MP");
        assertThat(send("POST", "/materiais", t, """
                {"codigo":"TIPO-X","descricao":"X","unidadeMedidaId":"%s","tipo":"QUALQUER"}"""
                .formatted(unidade)).statusCode()).as("tipo inválido").isEqualTo(400);
    }

    @Test
    void clientesCrudComCnpjOpcionalEUnico() throws Exception {
        String t = adminToken();

        var semCnpj = send("POST", "/clientes", t, """
                {"nome":"Cliente Sem CNPJ"}""");
        id(semCnpj, 201);
        assertThat(semCnpj.body()).doesNotContain("\"cnpj\"");

        var comMascara = send("POST", "/clientes", t, """
                {"nome":"Cliente CRUD","cnpj":"11.222.333/0001-81"}""");
        String id = id(comMascara, 201);
        assertThat(comMascara.body()).as("salvo só com os dígitos").contains("\"cnpj\":\"11222333000181\"");
        assertThat(send("POST", "/clientes", t, """
                {"nome":"Outro","cnpj":"11222333000181"}""").statusCode()).as("CNPJ duplicado").isEqualTo(409);
        assertThat(send("POST", "/clientes", t, """
                {"nome":"cliente crud"}""").statusCode()).as("nome duplicado").isEqualTo(409);
        assertThat(send("POST", "/clientes", t, """
                {"nome":"CNPJ ruim","cnpj":"11222333000100"}""").statusCode()).as("CNPJ inválido").isEqualTo(400);

        assertThat(send("PUT", "/clientes/" + id, t, """
                {"nome":"Cliente CRUD","cnpj":"11222333000181"}""").statusCode()).as("salvar sem trocar").isEqualTo(200);
        assertThat(send("GET", "/clientes?busca=222333", t, null).body()).as("busca pelo CNPJ").contains("Cliente CRUD");
        assertThat(send("GET", "/clientes?busca=crud", t, null).body()).contains("Cliente CRUD");

        assertThat(send("DELETE", "/clientes/" + id, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", "/clientes/" + id, t, null).statusCode()).isEqualTo(404);
    }

    @Test
    void ordensDeProducaoCrudEClienteEmUsoNaoSai() throws Exception {
        String t = adminToken();
        String unidade = id(send("POST", "/unidade-medida", t, """
                {"nome":"Peça OP","sigla":"pop"}"""), 201);
        String material = id(send("POST", "/materiais", t, """
                {"codigo":"OP-PA","descricao":"Chicote OP","unidadeMedidaId":"%s","tipo":"PRODUTO_ACABADO"}"""
                .formatted(unidade)), 201);
        String cliente = id(send("POST", "/clientes", t, """
                {"nome":"Cliente da OP"}"""), 201);

        String base = "/producao/operacoes/ordens-de-producao";
        var criada = send("POST", base, t, """
                {"codigo":"OP-0001","clienteId":"%s","materialId":"%s","quantidade":74,"previsao":"2026-11-10"}"""
                .formatted(cliente, material));
        String op = id(criada, 201);
        assertThat(criada.body()).contains("\"nome\":\"Cliente da OP\"").contains("\"codigo\":\"OP-PA\"");
        assertThat(send("POST", base, t, """
                {"codigo":"op-0001","clienteId":"%s","materialId":"%s","quantidade":1,"previsao":"2026-11-10"}"""
                .formatted(cliente, material)).statusCode()).as("código duplicado").isEqualTo(409);
        assertThat(send("POST", base, t, """
                {"codigo":"OP-0002","clienteId":"%s","materialId":"%s","quantidade":0,"previsao":"2026-11-10"}"""
                .formatted(cliente, material)).statusCode()).as("quantidade zero").isEqualTo(400);
        assertThat(send("POST", base, t, """
                {"codigo":"OP-0003","clienteId":"%s","materialId":"%s","quantidade":1,"previsao":"2026-11-10"}"""
                .formatted(java.util.UUID.randomUUID(), material)).statusCode()).as("cliente inexistente").isEqualTo(404);

        var editada = send("PUT", base + "/" + op, t, """
                {"codigo":"OP-0001","clienteId":"%s","materialId":"%s","quantidade":80,"previsao":"2026-11-12","etapaAtual":"Corte"}"""
                .formatted(cliente, material));
        assertThat(editada.statusCode()).as(editada.body()).isEqualTo(200);
        assertThat(editada.body()).contains("\"etapaAtual\":\"Corte\"");
        assertThat(send("GET", base + "?busca=chicote", t, null).body()).as("busca pelo material").contains("OP-0001");

        assertThat(send("DELETE", "/clientes/" + cliente, t, null).statusCode()).as("cliente com OP").isEqualTo(409);
        assertThat(send("DELETE", base + "/" + op, t, null).statusCode()).isEqualTo(200);
        assertThat(send("GET", base + "/" + op, t, null).statusCode()).isEqualTo(404);
        assertThat(send("DELETE", "/clientes/" + cliente, t, null).statusCode()).as("sem OPs, sai").isEqualTo(200);
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
