package br.lanx.api.cadastros.fornecedores;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.fornecedores.dtos.CreateFornecedorRequest;
import br.lanx.api.cadastros.fornecedores.dtos.FornecedorResponse;
import br.lanx.api.cadastros.fornecedores.dtos.UpdateFornecedorRequest;
import br.lanx.api.cadastros.fornecedores.exceptions.FornecedorAlreadyExistsException;
import br.lanx.api.cadastros.fornecedores.exceptions.FornecedorNotFoundException;
import br.lanx.api.cadastros.pais.Pais;
import br.lanx.api.cadastros.pais.PaisLookUpService;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class FornecedorService {

    private static final String NOME_DUPLICADO = "Já existe um fornecedor com esse nome.";

    private final FornecedorRepository fornecedorRepository;
    private final PaisLookUpService paisLookUpService;

    public FornecedorService(FornecedorRepository fornecedorRepository, PaisLookUpService paisLookUpService) {
        this.fornecedorRepository = fornecedorRepository;
        this.paisLookUpService = paisLookUpService;
    }

    @Transactional
    public FornecedorResponse create(CreateFornecedorRequest request) {
        String nome = request.nome().trim();
        if (fornecedorRepository.existsByNomeIgnoreCase(nome)) {
            throw new FornecedorAlreadyExistsException(NOME_DUPLICADO);
        }

        var fornecedor = new Fornecedor();
        fornecedor.setNome(nome);
        fornecedor.setPais(paisOuNulo(request.paisId()));
        return FornecedorResponse.fromEntity(fornecedorRepository.save(fornecedor));
    }

    @Transactional(readOnly = true)
    public FornecedorResponse findById(UUID id) {
        return FornecedorResponse.fromEntity(findOrThrow(id));
    }

    /** busca vazia = todos; senão, nome contendo o texto (sem diferenciar maiúsculas). */
    @Transactional(readOnly = true)
    public PageResult<FornecedorResponse> findAll(String busca, Pageable pageable) {
        var page = busca == null || busca.isBlank()
                ? fornecedorRepository.findAll(pageable)
                : fornecedorRepository.findByNomeContainingIgnoreCase(busca.trim(), pageable);
        return PageResult.from(page.map(FornecedorResponse::fromEntity));
    }

    @Transactional
    public FornecedorResponse update(UUID id, UpdateFornecedorRequest request) {
        Fornecedor fornecedor = findOrThrow(id);

        String nome = request.nome().trim();
        if (fornecedorRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new FornecedorAlreadyExistsException(NOME_DUPLICADO);
        }

        fornecedor.setNome(nome);
        fornecedor.setPais(paisOuNulo(request.paisId()));
        return FornecedorResponse.fromEntity(fornecedorRepository.save(fornecedor));
    }

    /** Exclusão lógica (@SQLDelete): itens de análise já importados continuam apontando para ele. */
    @Transactional
    public void delete(UUID id) {
        fornecedorRepository.delete(findOrThrow(id));
    }

    /**
     * Busca o fornecedor pelo nome ou cria um novo. O país é resolvido pelo nome
     * (sem diferenciar maiúsculas); se não for encontrado, o fornecedor fica sem
     * país.
     */
    @Transactional
    public Fornecedor findByNomeOrCreate(String nome, String nomePais) {
        Fornecedor fornecedor = fornecedorRepository.findByNome(nome).orElseGet(() -> criar(nome));
        if (fornecedor.getPais() == null && nomePais != null && !nomePais.isBlank()) {
            paisLookUpService.findOptionalByNome(nomePais.trim()).ifPresent(fornecedor::setPais);
        }
        return fornecedor;
    }

    private Fornecedor criar(String nome) {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(nome);
        return fornecedorRepository.save(fornecedor);
    }

    private Fornecedor findOrThrow(UUID id) {
        return fornecedorRepository.findById(id).orElseThrow(
                () -> new FornecedorNotFoundException("Fornecedor não encontrado: " + id));
    }

    private Pais paisOuNulo(UUID paisId) {
        return paisId == null ? null : paisLookUpService.findByIdOrThrow(paisId);
    }
}
