package br.lanx.api.cadastros.departamentos;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.departamentos.dtos.CreateDepartamentoRequest;
import br.lanx.api.cadastros.departamentos.dtos.DepartamentoResponse;
import br.lanx.api.cadastros.departamentos.dtos.UpdateDepartamentoRequest;
import br.lanx.api.cadastros.departamentos.exceptions.DepartamentoAlreadyExistsException;
import br.lanx.api.cadastros.departamentos.exceptions.DepartamentoInUseException;
import br.lanx.api.cadastros.departamentos.exceptions.DepartamentoNotFoundException;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class DepartamentoService {

    private static final String NOME_DUPLICADO = "Já existe um departamento com esse nome.";

    private final DepartamentoRepository departamentoRepository;
    private final List<DepartamentoUsageCheck> usageChecks;

    public DepartamentoService(DepartamentoRepository departamentoRepository,
            List<DepartamentoUsageCheck> usageChecks) {
        this.departamentoRepository = departamentoRepository;
        this.usageChecks = usageChecks;
    }

    @Transactional
    public DepartamentoResponse create(CreateDepartamentoRequest request) {
        String nome = request.nome().trim();
        if (departamentoRepository.existsByNomeIgnoreCase(nome)) {
            throw new DepartamentoAlreadyExistsException(NOME_DUPLICADO);
        }
        Departamento newDepartment = new Departamento();
        newDepartment.setNome(nome);
        return DepartamentoResponse.fromEntity(departamentoRepository.save(newDepartment));
    }

    /** busca vazia = todos; senão, nome contendo o texto (sem diferenciar maiúsculas). */
    @Transactional(readOnly = true)
    public PageResult<DepartamentoResponse> findAll(String busca, Pageable pageable) {
        var page = busca == null || busca.isBlank()
                ? departamentoRepository.findAll(pageable)
                : departamentoRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        return PageResult.from(page.map(DepartamentoResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public DepartamentoResponse findById(UUID id) {
        return DepartamentoResponse.fromEntity(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Departamento findByIdOrThrow(UUID id) {
        return departamentoRepository.findById(id).orElseThrow(
                () -> new DepartamentoNotFoundException("Não existe um departamento com esse ID: " + id));
    }

    @Transactional
    public DepartamentoResponse update(UUID id, UpdateDepartamentoRequest request) {
        Departamento departamento = findByIdOrThrow(id);

        // Ignora o próprio registro: salvar sem trocar o nome não é conflito
        String nome = request.nome().trim();
        if (departamentoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new DepartamentoAlreadyExistsException(NOME_DUPLICADO);
        }

        departamento.setNome(nome);
        return DepartamentoResponse.fromEntity(departamentoRepository.save(departamento));
    }

    @Transactional
    public void delete(UUID id) {
        if (!departamentoRepository.existsById(id)) {
            throw new DepartamentoNotFoundException("Não existe um departamento com esse ID: " + id);
        }
        if (usageChecks.stream().anyMatch(check -> check.isInUse(id))) {
            throw new DepartamentoInUseException();
        }
        departamentoRepository.deleteById(id);
    }

}
