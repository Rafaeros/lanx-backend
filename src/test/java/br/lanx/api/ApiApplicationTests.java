package br.lanx.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Mesma configuração dos testes de integração, para reaproveitar o contexto em cache: um contexto
 * diferente recriaria o H2 compartilhado (create-drop) e apagaria o admin dos testes seguintes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"app.bootstrap.admin.username=admin",
		"app.bootstrap.admin.password=Admin12345",
		"app.security.token.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@ActiveProfiles("test")
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
