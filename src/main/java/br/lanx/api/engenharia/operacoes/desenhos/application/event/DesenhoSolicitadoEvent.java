package br.lanx.api.engenharia.operacoes.desenhos.application.event;

import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoJob;

/**
 * O job foi salvo e precisa ir para o worker. Publicado dentro da transação; o adapter de mensageria só
 * manda para o RabbitMQ depois do commit (assim o worker nunca recebe um job que não existe no banco).
 * Serve tanto para o pedido novo quanto para o reprocessamento.
 */
public record DesenhoSolicitadoEvent(DesenhoJob job) {
}
