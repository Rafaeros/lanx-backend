package br.lanx.api.cadastros.shared;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Consulta de usuários do LANX para os vínculos de cadastros (comprador, operador). Implementada pelo
 * módulo auth: cadastros não depende de auth (auth já depende de cadastros).
 */
public interface UsuarioLookup {

    boolean exists(UUID userId);

    /** username de cada id encontrado; ids inexistentes ficam de fora. */
    Map<UUID, String> usernames(Collection<UUID> userIds);
}
