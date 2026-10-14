package br.lanx.api.cadastros.materiais;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.materiais.dtos.CreateMaterialRequest;
import br.lanx.api.cadastros.materiais.dtos.MaterialFilter;
import br.lanx.api.cadastros.materiais.dtos.MaterialResponse;
import br.lanx.api.cadastros.materiais.exceptions.MaterialAlreadyExistsException;
import br.lanx.api.cadastros.materiais.exceptions.MaterialUnidadeMedidaNaoEncontradaException;
import br.lanx.api.cadastros.unidade_medida.adapter.out.persistence.UnidadeMedidaEntity;
import br.lanx.api.cadastros.unidade_medida.application.port.in.ExistsUnidadeMedidaUseCase;
import br.lanx.api.shared.pagination.PageResult;
import jakarta.persistence.EntityManager;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ExistsUnidadeMedidaUseCase existsUnidadeMedida;
    private final EntityManager entityManager;

    public MaterialService(MaterialRepository materialRepository, ExistsUnidadeMedidaUseCase existsUnidadeMedida,
            EntityManager entityManager) {
        this.materialRepository = materialRepository;
        this.existsUnidadeMedida = existsUnidadeMedida;
        this.entityManager = entityManager;
    }

    @Transactional
    public MaterialResponse create(CreateMaterialRequest request) {
        if (materialRepository.existsByCodigo(request.codigo())) {
            throw new MaterialAlreadyExistsException("Já existe um material cadastrado com esse código.");
        }

        if (!existsUnidadeMedida.existsById(request.unidadeMedidaId())) {
            throw new MaterialUnidadeMedidaNaoEncontradaException("Unidade de medida não encontrada.");
        }

        // getReference cria uma proxy gerenciada com o ID, sem fazer SELECT no banco
        UnidadeMedidaEntity unidadeRef = entityManager.getReference(
                UnidadeMedidaEntity.class,
                request.unidadeMedidaId());

        Material material = new Material(
                request.codigo(),
                request.descricao(),
                unidadeRef);

        Material saved = materialRepository.save(material);
        return MaterialResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PageResult<MaterialResponse> findAll(MaterialFilter filter, Pageable pageable) {
        var spec = MaterialSpecifications.byFilter(filter);
        return PageResult.from(materialRepository.findAll(spec, pageable).map(MaterialResponse::fromEntity));
    }

}
