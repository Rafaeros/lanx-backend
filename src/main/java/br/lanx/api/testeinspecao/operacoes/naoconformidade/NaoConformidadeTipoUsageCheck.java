package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.tiponaoconformidade.TipoNaoConformidadeUsageCheck;

/** Não conformidades vetam a exclusão do tipo que ainda referenciam. */
@Component
class NaoConformidadeTipoUsageCheck implements TipoNaoConformidadeUsageCheck {

    private final NaoConformidadeRepository repository;

    NaoConformidadeTipoUsageCheck(NaoConformidadeRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isInUse(UUID tipoNaoConformidadeId) {
        return repository.existsByTipo_Id(tipoNaoConformidadeId);
    }
}
