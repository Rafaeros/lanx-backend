package br.lanx.api.cadastros.clientes;

import java.util.UUID;

/**
 * Quem usa clientes (ex.: ordens de produção) implementa isto para vetar a
 * exclusão de um cliente em uso. O cliente não conhece os módulos que o usam.
 */
public interface ClienteUsageCheck {
    boolean isInUse(UUID clienteId);
}
