package br.lanx.api.engenharia.operacoes.desenhos.adapter.in.messaging;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corpo da mensagem do worker (routing key {@code engenharia.desenho.resultado}). Campos ausentes chegam
 * {@code null}: o worker omite os vazios. Campos novos do worker são ignorados (não quebram o listener).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record DesenhoResultadoMessage(
        String jobId,
        String status,
        Zip zip,
        List<String> pendencias,
        String erro,
        Double duracaoSegundos) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Zip(String chave, String nomeOriginal, long tamanho) {
    }
}
