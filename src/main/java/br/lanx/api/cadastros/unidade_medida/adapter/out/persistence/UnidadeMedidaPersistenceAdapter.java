package br.lanx.api.cadastros.unidade_medida.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.lanx.api.cadastros.unidade_medida.application.port.out.UnidadeMedidaRepository;
import br.lanx.api.cadastros.unidade_medida.domain.UnidadeMedida;

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
    public Optional<UnidadeMedida> findByNome(String nome) {
        return jpa.findByNome(nome).map(UnidadeMedidaMapper::toDomain);
    }

    @Override
    public Optional<UnidadeMedida> findBySigla(String sigla) {
        return jpa.findBySigla(sigla).map(UnidadeMedidaMapper::toDomain);
    }

    @Override
    public boolean existsByNome(String nome) {
        return jpa.existsByNome(nome);
    }

    @Override
    public boolean existsBySigla(String sigla) {
        return jpa.existsBySigla(sigla);
    }
}
