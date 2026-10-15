package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
public class AnalisePedidoRestClient implements AnalisePedidoClient {

    private final RestClient restClient;

    public AnalisePedidoRestClient(@Value("${app.cg-api-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<RawItemPedido> buscar(LocalDate dataInicio, LocalDate dataFim) {
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/compras/analise-pedido-compras")
                        .queryParam("data_inicio", dataInicio)
                        .queryParam("data_fim", dataFim)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<AnalisePedidoComprasApiResponse>() {
                });

        int total = response != null && response.data() != null ? response.data().size() : 0;
        System.out.println(">>> cg-scraper retornou " + total + " itens para " + dataInicio + " a " + dataFim);

        return response == null || response.data() == null ? List.of() : response.data();
    }
}