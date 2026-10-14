package br.lanx.api.cadastros.unidade_medida.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import br.lanx.api.cadastros.unidade_medida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

@Repository
class UnidadeMedidaPersistenceAdapter implements UnidadeMedidaRepository {
    private final SpringDataUnidadeMedidaRepository jpa;

    UnidadeMedidaPersistenceAdapter(SpringDataUnidadeMedidaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public UnidadeMedida save(UnidadeMedida unidade) {
        UnidadeMedidaEntity entity = UnidadeMedidaMapper.toEntity(unidade);
        return UnidadeMedidaMapper.toDomain(jpa.save(entity));
    }

    @Override
    public PageResult<UnidadeMedida> search(String filtro, PageQuery query) {
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), Sort.by("nome"));
        Page<UnidadeMedidaEntity> page = jpa.search(filtro, pageRequest);

        return PageResult.from(page, UnidadeMedidaMapper::toDomain);
    }

    @Override
    public Optional<UnidadeMedida> findById(UUID id) {
        return jpa.findById(id).map(UnidadeMedidaMapper::toDomain);
    }

    @Override
    public Optional<UnidadeMedida> findByNome(String nome) {
        return jpa.findByNome(nome).map(UnidadeMedidaMapper::toDomain);
    }

    @Override
    public Optional<UnidadeMedida> findBySigla(String sigla) {
        return jpa.findBySigla(sigla).map(UnidadeMedidaMapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }

    @Override
    public boolean existsByNome(String nome) {
        return jpa.existsByNome(nome);
    }

    @Override
    public boolean existsBySigla(String sigla) {
        return jpa.existsBySigla(sigla);

    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }

}
