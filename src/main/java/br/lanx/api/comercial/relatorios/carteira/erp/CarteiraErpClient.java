package br.lanx.api.comercial.relatorios.carteira.erp;

import java.time.LocalDate;
import java.util.List;

/** Carteira consolidada do ERP (pedidos pendentes + etapa da OP + materiais faltantes), via cg-scraper. */
public interface CarteiraErpClient {
    List<CarteiraLinhaErp> buscar(LocalDate dataInicio, LocalDate dataFim);
}
