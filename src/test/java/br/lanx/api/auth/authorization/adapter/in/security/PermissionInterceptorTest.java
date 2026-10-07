package br.lanx.api.auth.authorization.adapter.in.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import br.lanx.api.auth.authorization.application.port.in.AuthorizeUseCase;
import br.lanx.api.auth.permission.domain.Action;
import br.lanx.api.auth.authorization.domain.exception.PermissionDeniedException;
import br.lanx.api.shared.authorization.FeatureKey;
import br.lanx.api.shared.authorization.RequiresAction;
import br.lanx.api.shared.authorization.RequiresFeature;
import br.lanx.api.shared.security.CurrentUser;
import br.lanx.api.shared.web.GlobalExceptionHandler;

class PermissionInterceptorTest {

    private static final UUID USER = UUID.randomUUID();
    private static final FeatureKey ANALISE = FeatureKey.of("compras.analise");

    @RestController
    @RequestMapping("/teste")
    @RequiresFeature("compras.analise")
    static class FakeController {
        @GetMapping
        String ler() {
            return "ok";
        }

        @DeleteMapping
        String apagar() {
            return "ok";
        }

        @PostMapping("/aprovar")
        @RequiresAction("aprovar")
        String aprovar() {
            return "ok";
        }

        @PostMapping("/outra")
        @RequiresFeature("estoque")
        String outraFuncionalidade() {
            return "ok";
        }
    }

    @RestController
    @RequestMapping("/livre")
    static class UnannotatedController {
        @GetMapping
        String livre() {
            return "ok";
        }
    }

    private AuthorizeUseCase authorizer;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        authorizer = mock(AuthorizeUseCase.class);
        CurrentUser currentUser = new CurrentUser() {
            @Override
            public UUID id() {
                return USER;
            }

            @Override
            public String username() {
                return "joao";
            }
        };
        mvc = MockMvcBuilders.standaloneSetup(new FakeController(), new UnannotatedController())
                .addInterceptors(new PermissionInterceptor(authorizer, currentUser))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("o verbo HTTP vira a ação CRUD")
    void verboViraAcao() throws Exception {
        mvc.perform(get("/teste")).andExpect(status().isOk());
        verify(authorizer).authorize(USER, ANALISE, Action.READ);

        mvc.perform(delete("/teste")).andExpect(status().isOk());
        verify(authorizer).authorize(USER, ANALISE, Action.DELETE);
    }

    @Test
    @DisplayName("sem permissão responde 403 e o controller nem executa")
    void negado() throws Exception {
        doThrow(new PermissionDeniedException()).when(authorizer).authorize(any(), any(), any());

        mvc.perform(get("/teste")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("@RequiresAction troca a checagem CRUD pela ação extra")
    void acaoExtra() throws Exception {
        mvc.perform(post("/teste/aprovar")).andExpect(status().isOk());

        verify(authorizer).authorizeAction(USER, ANALISE, "aprovar");
    }

    @Test
    @DisplayName("@RequiresFeature no método vence o da classe")
    void metodoVenceClasse() throws Exception {
        mvc.perform(post("/teste/outra")).andExpect(status().isOk());

        verify(authorizer).authorize(eq(USER), eq(FeatureKey.of("estoque")), eq(Action.CREATE));
    }

    @Test
    @DisplayName("rota sem anotação não consulta permissões")
    void semAnotacao() throws Exception {
        mvc.perform(get("/livre")).andExpect(status().isOk());

        verifyNoInteractions(authorizer);
        assertThat(PermissionInterceptor.actionFor("PATCH")).isEqualTo(Action.UPDATE);
    }
}
