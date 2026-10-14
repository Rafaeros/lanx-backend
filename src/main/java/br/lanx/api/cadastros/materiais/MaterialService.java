package br.lanx.api.cadastros.materiais;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.lanx.api.cadastros.materiais.dtos.CreateMaterialRequest;
import br.lanx.api.cadastros.materiais.dtos.MaterialFilter;
import br.lanx.api.cadastros.materiais.dtos.MaterialResponse;
import br.lanx.api.cadastros.materiais.dtos.UpdateMaterialRequest;
import br.lanx.api.cadastros.materiais.exceptions.MaterialAlreadyExistsException;
import br.lanx.api.cadastros.materiais.exceptions.MaterialNotFoundException;
import br.lanx.api.cadastros.materiais.exceptions.MaterialUnidadeMedidaNaoEncontradaException;
import br.lanx.api.cadastros.unidade_medida.adapter.out.persistence.UnidadeMedidaEntity;
import br.lanx.api.cadastros.unidade_medida.application.port.in.ExistsUnidadeMedidaUseCase;
import br.lanx.api.shared.pagination.PageResult;
import jakarta.persistence.EntityManager;

@Service
public class MaterialService {

    /** Resultado de findByCodigoOrCreate: o material e se ele acabou de ser criado. */
    public record FindOrCreateResult(MaterialResponse material, boolean created) {
    }

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
        String codigo = request.codigo().trim();
        if (materialRepository.existsByCodigo(codigo)) {
            throw new MaterialAlreadyExistsException("Já existe um material cadastrado com esse código.");
        }

        Material material = new Material(codigo, request.descricao().trim(), unidadeRef(request.unidadeMedidaId()));
        return MaterialResponse.fromEntity(materialRepository.save(material));
    }

    @Transactional(readOnly = true)
    public MaterialResponse findById(UUID id) {
        return materialRepository.findById(id)
                .map(MaterialResponse::fromEntity)
                .orElseThrow(() -> new MaterialNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public PageResult<MaterialResponse> findAll(MaterialFilter filter, Pageable pageable) {
        var spec = MaterialSpecifications.byFilter(filter);
        return PageResult.from(materialRepository.findAll(spec, pageable).map(MaterialResponse::fromEntity));
    }

    @Transactional
    public MaterialResponse update(UUID id, UpdateMaterialRequest request) {
        Material material = materialRepository.findById(id).orElseThrow(() -> new MaterialNotFoundException(id));

        String codigo = request.codigo().trim();
        if (materialRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new MaterialAlreadyExistsException("Já existe um material cadastrado com esse código.");
        }

        material.update(codigo, request.descricao().trim(), unidadeRef(request.unidadeMedidaId()));
        return MaterialResponse.fromEntity(materialRepository.save(material));
    }

    /** Exclusão lógica (@SQLDelete): o material some das consultas, a linha continua no banco. */
    @Transactional
    public void delete(UUID id) {
        if (!materialRepository.existsById(id)) {
            throw new MaterialNotFoundException(id);
        }
        materialRepository.deleteById(id);
    }

    /**
     * Devolve o material com esse código ou cria um novo com os dados informados.
     * Se já existe, os demais campos do pedido são ignorados (nada é atualizado).
     */
    @Transactional
    public FindOrCreateResult findByCodigoOrCreate(CreateMaterialRequest request) {
        return materialRepository.findByCodigo(request.codigo().trim())
                .map(existing -> new FindOrCreateResult(MaterialResponse.fromEntity(existing), false))
                .orElseGet(() -> new FindOrCreateResult(create(request), true));
    }

    private UnidadeMedidaEntity unidadeRef(UUID unidadeMedidaId) {
        if (!existsUnidadeMedida.existsById(unidadeMedidaId)) {
            throw new MaterialUnidadeMedidaNaoEncontradaException("Unidade de medida não encontrada.");
        }
        // getReference cria uma proxy gerenciada com o ID, sem fazer SELECT no banco
        return entityManager.getReference(UnidadeMedidaEntity.class, unidadeMedidaId);
    }
}
