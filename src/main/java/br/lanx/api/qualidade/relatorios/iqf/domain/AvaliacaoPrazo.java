
package br.lanx.api.qualidade.relatorios.iqf.domain;

import java.time.LocalDate;

/**
 * Como uma entrega conta no IQF de prazo, na data de referência (hoje).
 * <ul>
 * <li>Previsão depois de hoje: ainda não é avaliada, mesmo que já tenha sido
 * entregue (o IQF é montado dia a dia; senão meses futuros apareceriam com
 * 100%).</li>
 * <li>Entregue até N dias depois da previsão (tolerância, escolhida no filtro;
 * padrão {@value #TOLERANCIA_PADRAO_DIAS}, como na planilha de IQF da empresa):
 * no prazo. Cobre emissão de NF, transporte e recebimento.</li>
 * <li>Não entregue e a tolerância já passou: atrasada.</li>
 * </ul>
 */
public enum AvaliacaoPrazo {
    NO_PRAZO,
    ATRASADA,
    PENDENTE,
    NAO_AVALIAVEL;

    public static final int TOLERANCIA_PADRAO_DIAS = 1;
    public static final int TOLERANCIA_MAXIMA_DIAS = 30;

    /** Com a tolerância padrão. */
    public static AvaliacaoPrazo avaliar(
            LocalDate dataPrevista,
            LocalDate dataRealizada,
            LocalDate dataReferencia) {
        return avaliar(dataPrevista, dataRealizada, dataReferencia, TOLERANCIA_PADRAO_DIAS);
    }

    public static AvaliacaoPrazo avaliar(
            LocalDate dataPrevista,
            LocalDate dataRealizada,
            LocalDate dataReferencia,
            int toleranciaDias) {
        if (dataPrevista == null) {
            return NAO_AVALIAVEL;
        }

        if (dataPrevista.isAfter(dataReferencia)) {
            return PENDENTE;
        }

        LocalDate limite = dataPrevista.plusDays(toleranciaDias);

        if (dataRealizada != null) {
            return dataRealizada.isAfter(limite)
                    ? ATRASADA
                    : NO_PRAZO;
        }

        return limite.isBefore(dataReferencia) ? ATRASADA : PENDENTE;
    }
}
