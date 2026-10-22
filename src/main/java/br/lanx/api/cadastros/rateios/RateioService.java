package br.lanx.api.cadastros.rateios;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.rateios.dtos.RateioRequest;
import br.lanx.api.cadastros.rateios.dtos.RateioResponse;
import br.lanx.api.cadastros.rateios.exceptions.RateioAlreadyExistsException;
import br.lanx.api.cadastros.rateios.exceptions.RateioInUseException;
import br.lanx.api.cadastros.rateios.exceptions.RateioNotFoundException;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class RateioService {

    private static final String NOME_DUPLICADO = "Já existe um rateio com esse nome.";

    private final RateioRepository rateioRepository;
    private final List<RateioUsageCheck> usageChecks;

    public RateioService(RateioRepository rateioRepository, List<RateioUsageCheck> usageChecks) {
        this.rateioRepository = rateioRepository;
        this.usageChecks = usageChecks;
    }

    @Transactional
    public RateioResponse create(RateioRequest request) {
        String nome = request.nome().trim();
        if (rateioRepository.existsByNomeIgnoreCase(nome)) {
            throw new RateioAlreadyExistsException(NOME_DUPLICADO);
        }
        return RateioResponse.fromEntity(criar(nome));
    }

    /** busca vazia = todos; senão, nome contendo o texto (sem diferenciar maiúsculas). */
    @Transactional(readOnly = true)
    public PageResult<RateioResponse> findAll(String busca, Pageable pageable) {
        var page = busca == null || busca.isBlank()
                ? rateioRepository.findAll(pageable)
                : rateioRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        return PageResult.from(page.map(RateioResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public RateioResponse findById(UUID id) {
        return RateioResponse.fromEntity(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Rateio findByIdOrThrow(UUID id) {
        return rateioRepository.findById(id)
                .orElseThrow(() -> new RateioNotFoundException("Rateio não encontrado: " + id));
    }

    @Transactional
    public RateioResponse update(UUID id, RateioRequest request) {
        Rateio rateio = findByIdOrThrow(id);
        String nome = request.nome().trim();
        if (rateioRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new RateioAlreadyExistsException(NOME_DUPLICADO);
        }
        rateio.setNome(nome);
        return RateioResponse.fromEntity(rateioRepository.save(rateio));
    }

    /** Exclusão lógica; vetada enquanto houver itens importados apontando para o rateio. */
    @Transactional
    public void delete(UUID id) {
        Rateio rateio = findByIdOrThrow(id);
        if (usageChecks.stream().anyMatch(check -> check.isInUse(id))) {
            throw new RateioInUseException("Rateio em uso por itens importados e não pode ser excluído.");
        }
        rateioRepository.delete(rateio);
    }

    /** Usado na importação do ERP: busca pelo nome (sem diferenciar maiúsculas) ou cria. */
    @Transactional
    public Rateio findByNomeOrCreate(String nome) {
        String limpo = nome.trim();
        return rateioRepository.findByNomeIgnoreCase(limpo).orElseGet(() -> criar(limpo));
    }

    private Rateio criar(String nome) {
        Rateio rateio = new Rateio();
        rateio.setNome(nome);
        return rateioRepository.save(rateio);
    }
}
