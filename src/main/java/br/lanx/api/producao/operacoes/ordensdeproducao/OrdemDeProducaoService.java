package br.lanx.api.producao.operacoes.ordensdeproducao;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.clientes.ClienteService;
import br.lanx.api.cadastros.materiais.MaterialService;
import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.OrdemDeProducaoRequest;
import br.lanx.api.producao.operacoes.ordensdeproducao.dtos.OrdemDeProducaoResponse;
import br.lanx.api.producao.operacoes.ordensdeproducao.exceptions.OrdemDeProducaoAlreadyExistsException;
import br.lanx.api.producao.operacoes.ordensdeproducao.exceptions.OrdemDeProducaoNotFoundException;
import br.lanx.api.shared.pagination.PageResult;

@Service
public class OrdemDeProducaoService {

    private static final String CODIGO_DUPLICADO = "Já existe uma ordem de produção com esse código.";

    private final OrdemDeProducaoRepository repository;
    private final ClienteService clienteService;
    private final MaterialService materialService;

    public OrdemDeProducaoService(OrdemDeProducaoRepository repository, ClienteService clienteService,
            MaterialService materialService) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.materialService = materialService;
    }

    @Transactional
    public OrdemDeProducaoResponse create(OrdemDeProducaoRequest request) {
        String codigo = request.codigo().trim();
        if (repository.existsByCodigoIgnoreCase(codigo)) {
            throw new OrdemDeProducaoAlreadyExistsException(CODIGO_DUPLICADO);
        }
        OrdemDeProducao op = new OrdemDeProducao();
        preencher(op, codigo, request);
        return OrdemDeProducaoResponse.fromEntity(repository.save(op));
    }

    /** busca vazia = todas; senão, código da OP, cliente ou material contendo o texto. */
    @Transactional(readOnly = true)
    public PageResult<OrdemDeProducaoResponse> findAll(String busca, Pageable pageable) {
        String filtro = busca == null || busca.isBlank() ? "" : "%" + busca.trim().toLowerCase(Locale.ROOT) + "%";
        return PageResult.from(repository.buscar(filtro, pageable).map(OrdemDeProducaoResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public OrdemDeProducaoResponse findById(UUID id) {
        return OrdemDeProducaoResponse.fromEntity(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public OrdemDeProducao findByIdOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrdemDeProducaoNotFoundException("Ordem de produção não encontrada: " + id));
    }

    @Transactional
    public OrdemDeProducaoResponse update(UUID id, OrdemDeProducaoRequest request) {
        OrdemDeProducao op = findByIdOrThrow(id);
        String codigo = request.codigo().trim();
        if (repository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new OrdemDeProducaoAlreadyExistsException(CODIGO_DUPLICADO);
        }
        preencher(op, codigo, request);
        return OrdemDeProducaoResponse.fromEntity(repository.save(op));
    }

    /** Exclusão lógica (@SQLDelete): o código fica livre para uma nova OP. */
    @Transactional
    public void delete(UUID id) {
        repository.delete(findByIdOrThrow(id));
    }

    private void preencher(OrdemDeProducao op, String codigo, OrdemDeProducaoRequest request) {
        op.setCodigo(codigo);
        op.setCliente(clienteService.findByIdOrThrow(request.clienteId()));
        op.setMaterial(materialService.findByIdOrThrow(request.materialId()));
        op.setQuantidade(request.quantidade());
        op.setPrevisao(request.previsao());
        String etapa = request.etapaAtual();
        op.setEtapaAtual(etapa == null || etapa.isBlank() ? null : etapa.trim());
    }
}
