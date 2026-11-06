package br.lanx.api.engenharia.operacoes.desenhos.adapter.out.messaging;

import java.util.List;
import java.util.Map;

/**
 * Corpo da mensagem para o worker (contrato em {@code lanx-desenhos-worker/docs/contrato-mensagens.md}).
 * Só tipos simples: o JSON é o contrato entre Java e Python, não o domínio.
 */
record DesenhoSolicitadoMessage(
        String jobId,
        String projeto,
        ArquivoRef listaCorte,
        ArquivoRef desenhoCliente,
        Opcoes opcoes,
        Map<String, Object> projectOverride) {

    record ArquivoRef(String chave, String nomeOriginal) {
    }

    /** {@code formatos}: "a4"/"painel"; {@code acabamento}: a letra ou {@code null}. */
    record Opcoes(List<String> formatos, String acabamento, boolean vistas, boolean biblioteca, boolean dwg) {
    }
}
