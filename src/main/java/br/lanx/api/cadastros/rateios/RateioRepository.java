package br.lanx.api.cadastros.rateios;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateioRepository extends JpaRepository<Rateio, UUID> {

    Optional<Rateio> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    Page<Rateio> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
