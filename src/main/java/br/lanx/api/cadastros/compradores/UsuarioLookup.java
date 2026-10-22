package br.lanx.api.cadastros.compradores;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Consulta de usuários do LANX para o vínculo comprador ↔ usuário. Implementada pelo
 * módulo auth: cadastros não depende de auth (auth já depende de cadastros).
 */
public interface UsuarioLookup {

    boolean exists(UUID userId);

    /** username de cada id encontrado; ids inexistentes ficam de fora. */
    Map<UUID, String> usernames(Collection<UUID> userIds);
}
