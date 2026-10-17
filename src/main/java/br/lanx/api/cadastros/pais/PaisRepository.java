package br.lanx.api.cadastros.pais;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaisRepository extends JpaRepository<Pais, UUID> {
    Optional<Pais> findByNomeIgnoreCase(String nome);

    Optional<Pais> findByCodigoIso2(String codigoIso2);

    boolean existsByCodigoIso2(String codigoIso2);

    Optional<Pais> findByCodigoIso3(String codigoIso3);
}
