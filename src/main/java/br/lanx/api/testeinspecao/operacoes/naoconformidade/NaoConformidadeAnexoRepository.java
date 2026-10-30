package br.lanx.api.testeinspecao.operacoes.naoconformidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NaoConformidadeAnexoRepository extends JpaRepository<NaoConformidadeAnexo, UUID> {
    List<NaoConformidadeAnexo> findAllByNaoConformidade_IdOrderByCreatedAt(UUID naoConformidadeId);

    Optional<NaoConformidadeAnexo> findByIdAndNaoConformidade_Id(UUID id, UUID naoConformidadeId);
}
