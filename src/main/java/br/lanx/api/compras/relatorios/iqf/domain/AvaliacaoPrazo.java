
package br.lanx.api.compras.relatorios.iqf.domain;

import java.time.LocalDate;

public enum AvaliacaoPrazo {
    NO_PRAZO,
    ATRASADA,
    PENDENTE,
    NAO_AVALIAVEL;

    public static AvaliacaoPrazo avaliar(
            LocalDate dataPrevista,
            LocalDate dataRealizada,
            LocalDate dataReferencia) {
        if (dataPrevista == null) {
            return NAO_AVALIAVEL;
        }

        if (dataRealizada != null) {
            return dataRealizada.isAfter(dataPrevista)
                    ? ATRASADA
                    : NO_PRAZO;
        }

        if (dataPrevista.isBefore(dataReferencia)) {
            return ATRASADA;
        }

        return PENDENTE;
    }
}
