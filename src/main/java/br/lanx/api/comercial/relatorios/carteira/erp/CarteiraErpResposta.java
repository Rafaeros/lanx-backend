package br.lanx.api.comercial.relatorios.carteira.erp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record CarteiraErpResposta(boolean success, List<CarteiraLinhaErp> data) {
}
