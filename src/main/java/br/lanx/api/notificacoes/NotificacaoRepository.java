package br.lanx.api.notificacoes;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    long countByUserIdAndLidaEmIsNull(UUID userId);

    Page<Notificacao> findByUserIdAndLidaEmIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<Notificacao> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Notificacao> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select n.userId from Notificacao n
            where n.tipo = :tipo and n.referenciaId = :referenciaId
                """)
    Set<UUID> findUserIdsJaNotificados(@Param("tipo") TipoNotificacao tipo, @Param("referenciaId") UUID referenciaId);

    @Modifying
    @Query("""
            update Notificacao n set n.lidaEm = CURRENT_TIMESTAMP
            where n.userId = :userId and n.lidaEm is null
            """)
    int marcarTodasComoLidas(@Param("userId") UUID userId);

    
}
