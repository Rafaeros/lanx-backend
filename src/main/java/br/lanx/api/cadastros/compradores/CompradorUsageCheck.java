package br.lanx.api.cadastros.compradores;

import java.util.UUID;

/**
 * Quem usa compradores (ex.: itens da análise de pedidos) implementa isto para vetar
 * a exclusão de um comprador em uso. O comprador não conhece os módulos que o usam.
 */
public interface CompradorUsageCheck {
    boolean isInUse(UUID compradorId);
}
