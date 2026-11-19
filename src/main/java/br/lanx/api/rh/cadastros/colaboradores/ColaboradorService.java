package br.lanx.api.rh.cadastros.colaboradores;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.departamentos.DepartamentoService;
import br.lanx.api.cadastros.enderecos.Endereco;
import br.lanx.api.cadastros.enderecos.EnderecoService;
import br.lanx.api.rh.cadastros.colaboradores.dtos.ColaboradorDetailResponse;
import br.lanx.api.rh.cadastros.colaboradores.dtos.ColaboradorResponse;
import br.lanx.api.rh.cadastros.colaboradores.dtos.CreateColaboradorRequest;
import br.lanx.api.rh.cadastros.colaboradores.entities.Colaborador;
import br.lanx.api.rh.cadastros.colaboradores.exceptions.ColaboradorAlreadyExistsException;
import br.lanx.api.rh.cadastros.colaboradores.exceptions.ColaboradorNotFoundException;
import br.lanx.api.rh.cadastros.colaboradores.exceptions.PrincipalDuplicadoException;
import br.lanx.api.shared.pagination.PageResult;
import br.lanx.api.shared.security.crypto.CpfCryptoService;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;
    private final DepartamentoService departamentoService;
    private final EnderecoService enderecoService;
    private final CpfCryptoService cpfCryptoService;
    private final ColaboradorMapper mapper;

    public ColaboradorService(ColaboradorRepository colaboradorRepository, DepartamentoService departamentoService,
        EnderecoService enderecoService, CpfCryptoService cpfCryptoService, ColaboradorMapper mapper) {
        this.colaboradorRepository = colaboradorRepository;
        this.departamentoService = departamentoService;
        this.enderecoService = enderecoService;
        this.cpfCryptoService = cpfCryptoService;
        this.mapper = mapper;
    }

    @Transactional
    public ColaboradorResponse create(CreateColaboradorRequest req) {
        validarPrincipalUnico(req.telefones(), CreateColaboradorRequest.Telefone::principal, "telefone");
        validarPrincipalUnico(req.emails(), CreateColaboradorRequest.Email::principal, "email");
        validarPrincipalUnico(req.enderecos(), CreateColaboradorRequest.Endereco::principal, "endereço");

        String cpfHash = cpfCryptoService.hash(req.cpf());
        if (colaboradorRepository.existsByCpfHash(cpfHash)) {
            throw new ColaboradorAlreadyExistsException("Já existe um colaborador com esse CPF");
        }

        Departamento departamento = departamentoService.findByIdOrThrow(req.departamentoId());
        Colaborador colaborador = mapper.toEntity(req, departamento);
        colaborador.setCpfHash(cpfHash);
        colaborador.setCpfEncrypted(cpfCryptoService.encrypt(req.cpf()));

        req.enderecos().forEach(
            dto -> {
                Endereco endereco = enderecoService.create(dto.endereco());
                colaborador.adicionarEndereco(mapper.toColaboradorEndereco(endereco, dto));
            });

        return ColaboradorResponse.fromEntity(colaboradorRepository.save(colaborador));
    }

    @Transactional(readOnly = true)
    public PageResult<ColaboradorResponse> listAll(Pageable pageable) {
        return PageResult.from(colaboradorRepository.findAll(pageable).map(ColaboradorResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public ColaboradorDetailResponse findDetailById(UUID id) {
        Colaborador c = colaboradorRepository.findDetailById(id)
            .orElseThrow(
                () -> new ColaboradorNotFoundException("Não existe um colaborador com esse ID: " + id));

        return mapper.toDetail(c, cpfCryptoService.decrypt(c.getCpfEncrypted()));
    }

    private static <T> void validarPrincipalUnico(List<T> itens, Predicate<T> isPrincipal, String campo) {
        if (itens.stream().filter(isPrincipal).count() > 1) {
            throw new PrincipalDuplicadoException(campo);
        }
    }

    // TODO: update(id, UpdateColaboradorRequest): nome e departamento; se o CPF puder mudar, checar duplicidade
    //       com existsByCpfHashAndIdNot e recalcular hash + encrypt.
    // TODO: delete(id): findByIdOrThrow + repository.delete (vira soft delete pelo @SQLDelete).
    // TODO: adicionar/atualizar/remover endereço: buscar o vínculo pelo enderecoId na lista de endereços
    //       do colaborador, e devolver 404 se ele não pertencer a esse colaborador.
    // TODO: adicionar/atualizar/remover telefone e email, no mesmo padrão (adicionarX/removerX da entidade).
    // TODO: no update, validar principal único sobre a lista FINAL (o que está no banco + o que chegou);
    //       ao marcar um novo principal, desmarcar os outros e dar flush antes (índice único parcial no Postgres).
    // TODO: findByIdOrThrow(id) privado para reaproveitar em update/delete/sub-recursos.
    // TODO: listAll com filtro (nome, departamento).

}
