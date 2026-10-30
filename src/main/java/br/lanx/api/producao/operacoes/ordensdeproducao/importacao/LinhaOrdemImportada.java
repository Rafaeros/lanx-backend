package br.lanx.api.producao.operacoes.ordensdeproducao.importacao;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Uma linha válida da planilha de OPs do ERP. {@code status} ausente quando a
 * planilha não traz a coluna.
 */
record LinhaOrdemImportada(
        int linha,
        String codigo,
        String cliente,
        String materialCodigo,
        String materialDescricao,
        BigDecimal quantidade,
        LocalDate entrega,
        String status) {

    boolean finalizada() {
        return status != null && status.equalsIgnoreCase("Finalizada");
    }
}
