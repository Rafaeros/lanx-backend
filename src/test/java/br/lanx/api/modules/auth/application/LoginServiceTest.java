package br.lanx.api.modules.auth.application;

import br.lanx.api.modules.auth.domain.User;
import br.lanx.api.modules.auth.domain.exceptions.InvalidCredentialException;
import br.lanx.api.modules.auth.domain.exceptions.UserInactiveException;
import br.lanx.api.modules.auth.fakes.FakePasswordHasher;
import br.lanx.api.modules.auth.fakes.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginServiceTest {

    private static final Instant EXPIRES_AT = Instant.parse("2026-10-06T23:00:00Z");

    private InMemoryUserRepository users;   // FAKE
    private TokenIssuer tokenIssuer;        // MOCK (Mockito)
    private LoginService loginService;      // o que está sendo testado ("SUT")

    @BeforeEach   // roda antes de CADA teste: cada um começa do zero
    void setUp() {
        users = new InMemoryUserRepository();
        tokenIssuer = mock(TokenIssuer.class);
        loginService = new LoginService(users, new FakePasswordHasher(), tokenIssuer);
        //                ↑ "new" direto: sem Spring, sem @Autowired
    }

    @Test
    @DisplayName("credenciais corretas e usuário ativo → devolve o token")
    void loginComSucesso() {
        // Arrange (prepara o cenário)
        User joao = usuario("joao", "senha123", true);
        users.save(joao);
        when(tokenIssuer.issue(joao))                                  // STUB: "quando chamarem issue(joao)..."
                .thenReturn(new TokenIssuer.Token("jwt-fake", EXPIRES_AT));

        // Act (executa)
        LoginResult result = loginService.login(new LoginCommand("joao", "senha123"));

        // Assert (verifica)
        assertThat(result.accessToken()).isEqualTo("jwt-fake");
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
        verify(tokenIssuer).issue(joao);                               // MOCK: foi chamado exatamente 1 vez
    }

    @Test
    @DisplayName("senha errada → InvalidCredential e nenhum token emitido")
    void senhaErrada() {
        users.save(usuario("joao", "senha123", true));

        assertThatThrownBy(() -> loginService.login(new LoginCommand("joao", "errada")))
                .isInstanceOf(InvalidCredentialException.class);

        verify(tokenIssuer, never()).issue(any());                     // garante que NÃO emitiu
    }

    @Test
    @DisplayName("usuário inexistente → MESMA exceção e mensagem da senha errada")
    void usuarioInexistente() {
        assertThatThrownBy(() -> loginService.login(new LoginCommand("fantasma", "qualquer")))
                .isInstanceOf(InvalidCredentialException.class)
                .hasMessage("Usuário ou senha inválidos");             // não revela que o user não existe
    }

    @Test
    @DisplayName("usuário inativo com senha correta → UserInactive")
    void usuarioInativo() {
        users.save(usuario("maria", "senha123", false));

        assertThatThrownBy(() -> loginService.login(new LoginCommand("maria", "senha123")))
                .isInstanceOf(UserInactiveException.class);

        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    @DisplayName("usuário inativo com senha ERRADA → InvalidCredential (não revela que a conta existe)")
    void inativoComSenhaErrada() {
        users.save(usuario("maria", "senha123", false));

        assertThatThrownBy(() -> loginService.login(new LoginCommand("maria", "errada")))
                .isInstanceOf(InvalidCredentialException.class);
    }

    // ---------- helper: deixa os testes curtos e legíveis ----------
    private static User usuario(String username, String senha, boolean ativo) {
        return new User(UUID.randomUUID(), username, "hashed:" + senha, ativo, Set.of());
    }
}