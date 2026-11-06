package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoDetalhe;
import br.lanx.api.engenharia.operacoes.desenhos.application.dto.DesenhoResumo;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.ConsultarDesenhosPort;
import br.lanx.api.engenharia.operacoes.desenhos.domain.StatusDesenho;
import br.lanx.api.shared.pagination.PageQuery;
import br.lanx.api.shared.pagination.PageResult;

/** Leitura: Entity → read model direto, sem passar pelo domínio (não há regra para proteger). */
@Repository
class DesenhoJobQueryAdapter implements ConsultarDesenhosPort {

    private final SpringDataDesenhoJobRepository jpa;
    private final DesenhoJobMapper mapper;

    DesenhoJobQueryAdapter(SpringDataDesenhoJobRepository jpa, DesenhoJobMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public PageResult<DesenhoResumo> listar(UUID solicitanteId, StatusDesenho status, String projeto,
            PageQuery pagina) {
        var pageable = PageRequest.of(pagina.page(), pagina.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResult.from(
                jpa.findAll(SpringDataDesenhoJobRepository.filtro(solicitanteId, status, projeto), pageable),
                mapper::toResumo);
    }

    @Override
    public Optional<DesenhoDetalhe> detalhe(UUID jobId) {
        return jpa.findById(jobId).map(mapper::toDetalhe);
    }
}
