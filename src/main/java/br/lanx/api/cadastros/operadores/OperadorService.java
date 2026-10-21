package br.lanx.api.cadastros.operadores;

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
import br.lanx.api.shared.pagination.PageResult;

@Service
public class OperadorService {

    private static final String NOME_DUPLICADO = "Já existe um operador com esse nome.";

    private final OperadorRepository operadorRepository;
    private final DepartamentoService departamentoService;

    public OperadorService(OperadorRepository operadorRepository, DepartamentoService departamentoService) {
        this.operadorRepository = operadorRepository;
        this.departamentoService = departamentoService;
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
        return OperadorResponse.fromEntity(operadorRepository.save(newOperador));
    }

    /** busca vazia = todos; senão, nome contendo o texto (sem diferenciar maiúsculas). */
    @Transactional(readOnly = true)
    public PageResult<OperadorResponse> findAll(String busca, Pageable pageable) {
        var page = busca == null || busca.isBlank()
                ? operadorRepository.findAll(pageable)
                : operadorRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        return PageResult.from(page.map(OperadorResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public OperadorResponse findById(UUID id) {
        return OperadorResponse.fromEntity(findByIdOrThrow(id));
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
        return OperadorResponse.fromEntity(operadorRepository.save(existingOperador));
    }

    @Transactional
    public void delete(UUID id) {
        Operador operador = findByIdOrThrow(id);
        operadorRepository.delete(operador);
    }

    private Departamento departamentoOuNulo(UUID departamentoId) {
        return departamentoId == null ? null : departamentoService.findByIdOrThrow(departamentoId);
    }
}
