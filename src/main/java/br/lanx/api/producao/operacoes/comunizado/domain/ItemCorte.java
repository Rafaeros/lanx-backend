package br.lanx.api.producao.operacoes.comunizado.domain;

import java.math.BigDecimal;

/**
 * Um circuito da lista de corte. {@code gravacao} é o texto gravado no cabo
 * (coluna GRAVAÇÃO, só em algumas ITs); nulo quando a IT não tem.
 */
public record ItemCorte(
    String produto,
    String operador,
    String circuito,
    String gravacao,
    BigDecimal comprimento,
    String tipo,
    String terminalA,
    BigDecimal desponteA,
    String terminalB,
    BigDecimal desponteB
){}
