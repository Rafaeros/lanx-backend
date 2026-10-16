package br.lanx.api.compras.relatorios.analisepedidos.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Lead time do pedido: dias entre a emissão do PC e a entrega. Sem entrega, ainda não há lead time. */
public final class LeadTime {

    private LeadTime() {
    }

    public static Long emDias(LocalDate emissaoPc, LocalDate entregaPc) {
        if (emissaoPc == null || entregaPc == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(emissaoPc, entregaPc);
    }
}
