package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.math.BigDecimal;

/**
 * Situação de atendimento de um item de pedido. A mesma regra está replicada no
 * filtro JPQL de AnalisePedidoRepository: mudou aqui, mude lá.
 */
public enum Situacao {
    /** Nada recebido ainda. */
    PENDENTE,
    /** Recebido em parte: ainda há saldo. */
    PARCIAL,
    /** Recebido por completo: sem saldo. */
    ATENDIDO;

    public static Situacao de(BigDecimal quantidadeAtendida, BigDecimal saldo) {
        BigDecimal atendida = quantidadeAtendida == null ? BigDecimal.ZERO : quantidadeAtendida;
        BigDecimal restante = saldo == null ? BigDecimal.ZERO : saldo;
        if (atendida.signum() <= 0) {
            return PENDENTE;
        }
        return restante.signum() > 0 ? PARCIAL : ATENDIDO;
    }
}
