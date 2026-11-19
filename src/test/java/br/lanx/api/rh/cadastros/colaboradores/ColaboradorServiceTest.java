package br.lanx.api.rh.cadastros.colaboradores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.departamentos.DepartamentoService;
import br.lanx.api.cadastros.enderecos.EnderecoService;
import br.lanx.api.cadastros.enderecos.dtos.EnderecoRequest;
import br.lanx.api.rh.cadastros.colaboradores.dtos.ColaboradorResponse;
import br.lanx.api.rh.cadastros.colaboradores.dtos.CreateColaboradorRequest;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoEmail;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoEndereco;
import br.lanx.api.rh.cadastros.colaboradores.enums.TipoTelefone;
import br.lanx.api.rh.cadastros.colaboradores.exceptions.ColaboradorAlreadyExistsException;
import br.lanx.api.rh.cadastros.colaboradores.exceptions.PrincipalDuplicadoException;
import br.lanx.api.shared.security.crypto.CpfCryptoService;

@ExtendWith(MockitoExtension.class)
public class ColaboradorServiceTest {

    private static final String CPF = "52998224725";
    private static final String TELEFONE = "+5543984256314";

    @Mock
    ColaboradorRepository repository;
    @Mock
    DepartamentoService departamentoService;
    @Mock
    EnderecoService enderecoService;
    @Mock
    CpfCryptoService crypto;

    ColaboradorService service;

    @BeforeEach
    void setUp() {
        service = new ColaboradorService(repository, departamentoService,
            enderecoService, crypto, new ColaboradorMapper());
    }

    @Test
    void cpf_duplicado_lanca_conflito() {
        // Arrange (prepara)
        when(crypto.hash(CPF)).thenReturn("hash");
        when(repository.existsByCpfHash("hash")).thenReturn(true);

        var req = requestMinimo();

        // Act + Assert (execute e confere)
        assertThatThrownBy(
            () -> service.create(req)).isInstanceOf(ColaboradorAlreadyExistsException.class)
            .hasMessage("Já existe um colaborador com esse CPF");

        verify(repository, never()).save(any());
    }

    @Test
    void criar_colaborador_e_criptografa_cpf() {
        var departamento = departamento();
        var req = requestMinimo();

        when(crypto.hash(CPF)).thenReturn("hash");
        when(repository.existsByCpfHash("hash")).thenReturn(false);
        when(departamentoService.findByIdOrThrow(any())).thenReturn(departamento); // só passa o objeto pronto
        when(crypto.encrypt(CPF)).thenReturn("cpf-criptografado");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ColaboradorResponse resp = service.create(req);

        assertThat(resp.nome()).isEqualTo("Ana Souza");
        assertThat(resp.departamentoNome()).isEqualTo("Recursos Humanos");
        verify(repository)
            .save(argThat(c -> c.getCpfHash().equals("hash") && c.getCpfEncrypted().equals("cpf-criptografado")));
    }

    @Test
    void dois_enderecos_principais_lancam_validacao() {

        // Arrange
        var req = requestEndereco();

        // Act + Assert
        assertThatThrownBy(
            () -> service.create(req)).isInstanceOf(PrincipalDuplicadoException.class)
            .hasMessage("Só é permitido um endereço principal");

        // Assert
        verifyNoInteractions(repository, departamentoService, enderecoService, crypto);
    }

    @Test
    void dois_emails_principais_lancam_validacao() {
        var req = requestEmail();

        assertThatThrownBy(
            () -> service.create(req))
            .isInstanceOf(PrincipalDuplicadoException.class)
            .hasMessage("Só é permitido um email principal");

        verifyNoInteractions(repository, departamentoService, enderecoService, crypto);
    }

    @Test
    void dois_telefones_principais_lancam_validacao() {
        var req = requestTelefone();

        assertThatThrownBy(
            () -> service.create(req)).isInstanceOf(PrincipalDuplicadoException.class)
            .hasMessage("Só é permitido um telefone principal");

        verifyNoInteractions(repository, departamentoService, enderecoService, crypto);
    }

    private CreateColaboradorRequest requestMinimo() {
        return new CreateColaboradorRequest(
            "Ana Souza", // nome
            CPF, // cpf
            UUID.randomUUID(), // departamentoId
            List.of(), // telefones
            List.of(), // emails
            List.of()); // enderecos
    }

    private CreateColaboradorRequest requestTelefone() {
        return new CreateColaboradorRequest(
            "Ana Souza",
            CPF,
            UUID.randomUUID(),
            List.of(
                colaboradorTelefone1(),
                colaboradorTelefone2()),
            List.of(),
            List.of());
    }

    private CreateColaboradorRequest requestEmail() {
        return new CreateColaboradorRequest(
            "Ana Souza",
            CPF,
            UUID.randomUUID(),
            List.of(),
            List.of(
                colaboradorEmail1(),
                colaboradorEmail2()),
            List.of());
    }

    private CreateColaboradorRequest requestEndereco() {
        return new CreateColaboradorRequest(
            "Ana Souza",
            CPF,
            UUID.randomUUID(),
            List.of(),
            List.of(),
            List.of(
                colaboradorEndereco1(),
                colaboradorEndereco2()));
    }

    private CreateColaboradorRequest.Telefone colaboradorTelefone1() {
        return new CreateColaboradorRequest.Telefone(
            TELEFONE,
            TipoTelefone.CELULAR,
            true);
    }

    private CreateColaboradorRequest.Telefone colaboradorTelefone2() {
        return new CreateColaboradorRequest.Telefone(
            TELEFONE,
            null,
            false);
    }

    private CreateColaboradorRequest.Email colaboradorEmail1() {
        return new CreateColaboradorRequest.Email(
            "anasouza@gmail.com",
            TipoEmail.PESSOAL,
            true);
    }

    private CreateColaboradorRequest.Email colaboradorEmail2() {
        return new CreateColaboradorRequest.Email(
            "anasouza@lanxcables.com.br",
            null,
            true);
    }

    private EnderecoRequest endereco1() {
        return new EnderecoRequest(
            45495,
            "86065-000",
            "Avenida Arthut Thomas",
            "1795",
            "Galpão",
            "Parque Jamaica");
    }

    private CreateColaboradorRequest.Endereco colaboradorEndereco1() {
        var end = endereco1();
        return new CreateColaboradorRequest.Endereco(
            end,
            TipoEndereco.RESIDENCIAL,
            true);
    }

    private CreateColaboradorRequest.Endereco colaboradorEndereco2() {
        var end = endereco1();
        return new CreateColaboradorRequest.Endereco(end, null, true);
    }

    private Departamento departamento() {
        Departamento dep = mock(Departamento.class);
        when(dep.getNome()).thenReturn("Recursos Humanos");
        return dep;
    }

    // TODO: corrigir dois_telefones_principais_lancam_validacao: o colaboradorTelefone2() está com
    //       principal = false, então a validação passa e o teste quebra com NullPointerException.
    // TODO: um_principal_e_um_nao_principal_e_permitido (o limite da regra), com when no enderecoService.create.
    // TODO: criar_com_endereco_associa_vinculo: argThat no save conferindo endereço, tipo (default RESIDENCIAL) e principal.
    // TODO: find_detail_id_inexistente_lanca_not_found (findDetailById -> Optional.empty()).
    // TODO: find_detail_descriptografa_cpf (when crypto.decrypt + assertThat no cpf do response).
    // TODO: testes de update/delete e dos sub-recursos quando forem implementados.
    // TODO: CpfCryptoServiceTest: decrypt(encrypt(x)) == x, hash determinístico, CPF com e sem máscara.
    // TODO: ColaboradorCrudIntegrationTest (POST -> GET /{id}, 409, 400, 404, 401/403). Antes disso:
    //       adicionar "CREATE SCHEMA IF NOT EXISTS recursos_humanos" no INIT do H2 (application-test.yaml),
    //       definir app.security.crypto.aes-key/hmac-key no perfil de teste e inserir uma Cidade no setup.
}