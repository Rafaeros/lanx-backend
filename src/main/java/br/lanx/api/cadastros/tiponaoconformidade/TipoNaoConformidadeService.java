package br.lanx.api.cadastros.tiponaoconformidade;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.tiponaoconformidade.dtos.CreateTipoNaoConformidadeRequest;
import br.lanx.api.cadastros.tiponaoconformidade.dtos.TipoNaoConformidadeResponse;
import br.lanx.api.cadastros.tiponaoconformidade.dtos.UpdateTipoNaoConformidadeRequest;
import br.lanx.api.cadastros.tiponaoconformidade.exceptions.TipoNaoConformidadeAlreadyExistsException;
import br.lanx.api.cadastros.tiponaoconformidade.exceptions.TipoNaoConformidadeInUseException;
import br.lanx.api.cadastros.tiponaoconformidade.exceptions.TipoNaoConformidadeNotFoundException;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class TipoNaoConformidadeService {

    private static final String NOME_DUPLICADO = "Já existe um tipo de não conformidade com esse nome.";

    private final TipoNaoConformidadeRepository tipoNaoConformidadeRepository;
    private final List<TipoNaoConformidadeUsageCheck> usageChecks;

    public TipoNaoConformidadeService(TipoNaoConformidadeRepository tipoNaoConformidadeRepository,
            List<TipoNaoConformidadeUsageCheck> usageChecks) {
        this.tipoNaoConformidadeRepository = tipoNaoConformidadeRepository;
        this.usageChecks = usageChecks;
    }

    @Transactional
    public TipoNaoConformidadeResponse create(CreateTipoNaoConformidadeRequest request) {
        String nome = request.nome().trim();
        if (tipoNaoConformidadeRepository.existsByNomeIgnoreCase(nome)) {
            throw new TipoNaoConformidadeAlreadyExistsException(NOME_DUPLICADO);
        }
        TipoNaoConformidade tipo = new TipoNaoConformidade();
        tipo.setNome(nome);
        return TipoNaoConformidadeResponse.fromEntity(tipoNaoConformidadeRepository.save(tipo));
    }

    /** busca vazia = todos; senão, nome contendo o texto (sem diferenciar maiúsculas). */
    @Transactional(readOnly = true)
    public PageResult<TipoNaoConformidadeResponse> findAll(String busca, Pageable pageable) {
        var page = busca == null || busca.isBlank()
                ? tipoNaoConformidadeRepository.findAll(pageable)
                : tipoNaoConformidadeRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        return PageResult.from(page.map(TipoNaoConformidadeResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public TipoNaoConformidadeResponse findById(UUID id) {
        return TipoNaoConformidadeResponse.fromEntity(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public TipoNaoConformidade findByIdOrThrow(UUID id) {
        return tipoNaoConformidadeRepository.findById(id).orElseThrow(
                () -> new TipoNaoConformidadeNotFoundException("Não existe um tipo de não conformidade com esse ID: " + id));
    }

    @Transactional
    public TipoNaoConformidadeResponse update(UUID id, UpdateTipoNaoConformidadeRequest request) {
        TipoNaoConformidade tipo = findByIdOrThrow(id);

        // Ignora o próprio registro: salvar sem trocar o nome não é conflito
        String nome = request.nome().trim();
        if (tipoNaoConformidadeRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new TipoNaoConformidadeAlreadyExistsException(NOME_DUPLICADO);
        }

        tipo.setNome(nome);
        return TipoNaoConformidadeResponse.fromEntity(tipoNaoConformidadeRepository.save(tipo));
    }

    @Transactional
    public void delete(UUID id) {
        if (!tipoNaoConformidadeRepository.existsById(id)) {
            throw new TipoNaoConformidadeNotFoundException("Não existe um tipo de não conformidade com esse ID: " + id);
        }
        if (usageChecks.stream().anyMatch(check -> check.isInUse(id))) {
            throw new TipoNaoConformidadeInUseException();
        }
        tipoNaoConformidadeRepository.deleteById(id);
    }
}
