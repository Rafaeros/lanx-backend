package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.departamentos.DepartamentoUsageCheck;

/** Não conformidades vetam a exclusão do departamento que ainda referenciam. */
@Component
class NaoConformidadeDepartamentoUsageCheck implements DepartamentoUsageCheck {

    private final NaoConformidadeRepository repository;

    NaoConformidadeDepartamentoUsageCheck(NaoConformidadeRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isInUse(UUID departamentoId) {
        return repository.existsByDepartamento_Id(departamentoId);
    }
}
