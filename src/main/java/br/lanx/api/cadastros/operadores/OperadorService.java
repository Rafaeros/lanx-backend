package br.lanx.api.cadastros.operadores;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.departamentos.Departamento;
import br.lanx.api.cadastros.departamentos.DepartamentoService;
import br.lanx.api.cadastros.operadores.dtos.CreateOperadorRequest;
import br.lanx.api.cadastros.operadores.dtos.OperadorResponse;
import br.lanx.api.cadastros.operadores.dtos.UpdateOperadorRequest;
import br.lanx.api.cadastros.operadores.exceptions.OperadorAlreadyExistsException;
import br.lanx.api.cadastros.operadores.exceptions.OperadorNotFoundException;
import br.lanx.api.cadastros.shared.UsuarioLookup;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class OperadorService {

    private static final String NOME_DUPLICADO = "Já existe um operador com esse nome.";

    private final OperadorRepository operadorRepository;
    private final DepartamentoService departamentoService;
    private final UsuarioLookup usuarioLookup;

    public OperadorService(OperadorRepository operadorRepository, DepartamentoService departamentoService,
            UsuarioLookup usuarioLookup) {
        this.operadorRepository = operadorRepository;
        this.departamentoService = departamentoService;
        this.usuarioLookup = usuarioLookup;
    }

    @Transactional
    public OperadorResponse create(CreateOperadorRequest request) {
        String nome = request.nome().trim();
        if (operadorRepository.existsByNomeIgnoreCase(nome)) {
            throw new OperadorAlreadyExistsException(NOME_DUPLICADO);
        }

        Operador newOperador = new Operador();
        newOperador.setNome(nome);
        newOperador.assignDepartamento(departamentoOuNulo(request.departamentoId()));
        newOperador.assignUser(usuarioValido(request.userId(), null));
        return toResponse(operadorRepository.save(newOperador));
    }

    /** busca vazia = todos; senão, nome contendo o texto (sem diferenciar maiúsculas). */
    @Transactional(readOnly = true)
    public PageResult<OperadorResponse> findAll(String busca, Pageable pageable) {
        var page = busca == null || busca.isBlank()
                ? operadorRepository.findAll(pageable)
                : operadorRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        // usernames da página numa consulta só
        Map<UUID, String> usernames = usuarioLookup.usernames(
                page.getContent().stream().map(Operador::getUserId).filter(Objects::nonNull).toList());
        return PageResult.from(page.map(o -> OperadorResponse.of(o, username(usernames, o.getUserId()))));
    }

    @Transactional(readOnly = true)
    public OperadorResponse findById(UUID id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Operador findByIdOrThrow(UUID id) {
        return operadorRepository.findById(id).orElseThrow(
                () -> new OperadorNotFoundException("Não foi encontrado nenhum operador com esse id: " + id));
    }

    @Transactional
    public OperadorResponse update(UUID id, UpdateOperadorRequest request) {
        Operador existingOperador = findByIdOrThrow(id);

        String nome = request.nome().trim();
        if (operadorRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new OperadorAlreadyExistsException(NOME_DUPLICADO);
        }

        existingOperador.setNome(nome);
        existingOperador.assignDepartamento(departamentoOuNulo(request.departamentoId()));
        existingOperador.assignUser(usuarioValido(request.userId(), id));
        return toResponse(operadorRepository.save(existingOperador));
    }

    @Transactional
    public void delete(UUID id) {
        Operador operador = findByIdOrThrow(id);
        operadorRepository.delete(operador);
    }

    /** Usuário existente e ainda sem operador (o índice uk_operadores_user garante o mesmo no banco). */
    private UUID usuarioValido(UUID userId, UUID operadorId) {
        if (userId == null) {
            return null;
        }
        if (!usuarioLookup.exists(userId)) {
            throw new OperadorNotFoundException("Usuário não encontrado: " + userId);
        }
        boolean emUso = operadorId == null
                ? operadorRepository.existsByUserId(userId)
                : operadorRepository.existsByUserIdAndIdNot(userId, operadorId);
        if (emUso) {
            throw new OperadorAlreadyExistsException("Esse usuário já está vinculado a outro operador.");
        }
        return userId;
    }

    /** Operador sem vínculo não consulta o mapa (alguns Map recusam chave nula). */
    private static String username(Map<UUID, String> usernames, UUID userId) {
        return userId == null ? null : usernames.get(userId);
    }

    private OperadorResponse toResponse(Operador operador) {
        UUID userId = operador.getUserId();
        return OperadorResponse.of(operador, username(usuarioLookup.usernames(
                userId == null ? List.of() : List.of(userId)), userId));
    }

    private Departamento departamentoOuNulo(UUID departamentoId) {
        return departamentoId == null ? null : departamentoService.findByIdOrThrow(departamentoId);
    }
}
