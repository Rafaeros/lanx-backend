package br.lanx.api.rh.cadastros.colaboradores;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.lanx.api.rh.cadastros.colaboradores.entities.Colaborador;

public interface ColaboradorRepository extends JpaRepository<Colaborador, UUID> {

    @EntityGraph(attributePaths = "departamento")
    Page<Colaborador> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "departamento",
            "enderecos",
            "enderecos.endereco",
            "enderecos.endereco.cidade"
    })
    Optional<Colaborador> findDetailById(UUID id);

    boolean existsByCpfHash(String cpfHash);

    // TODO: existsByCpfHashAndIdNot(String cpfHash, UUID id) para o update não acusar o próprio colaborador.
    // TODO: busca paginada com filtro (nome contém, departamentoId) mantendo o @EntityGraph("departamento").
}
