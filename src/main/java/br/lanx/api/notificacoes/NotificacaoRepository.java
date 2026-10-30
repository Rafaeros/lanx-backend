package br.lanx.api.notificacoes;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    Page<Notificacao> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    


    @Query("""
            select n.userId from Notificacao n
            where n.tipo = :tipo and n.referenciaId = :referenciaId
                """)
    Set<UUID> findUserIdsJaNotificados(@Param("tipo") TipoNotificacao tipo, @Param("referenciaId") UUID referenciaId);

}
