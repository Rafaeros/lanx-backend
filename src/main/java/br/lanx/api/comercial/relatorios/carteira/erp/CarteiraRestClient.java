package br.lanx.api.comercial.relatorios.carteira.erp;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.lanx.api.comercial.relatorios.carteira.exceptions.CarteiraErpIndisponivelException;

/**
 * O cg-scraper monta a carteira na hora, cruzando três relatórios do ERP: um
 * ano leva ~1,5 min. Por isso o tempo de leitura é longo (app.cg-carteira-timeout)
 * e só a conexão falha rápido (scraper fora do ar).
 */
@Component
class CarteiraRestClient implements CarteiraErpClient {

    private static final Logger log = LoggerFactory.getLogger(CarteiraRestClient.class);

    private final RestClient restClient;

    CarteiraRestClient(@Value("${app.cg-api-url}") String baseUrl,
            @Value("${app.cg-carteira-timeout}") Duration timeout) {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var requestFactory = new JdkClientHttpRequestFactory(http);
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<CarteiraLinhaErp> buscar(LocalDate dataInicio, LocalDate dataFim) {
        try {
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/relatorios/carteira")
                            .queryParam("data_inicio", dataInicio)
                            .queryParam("data_fim", dataFim)
                            .build())
                    .retrieve()
                    .body(CarteiraErpResposta.class);
            return response == null || response.data() == null ? List.of() : response.data();
        } catch (ResourceAccessException e) {
            log.warn("cg-scraper sem resposta para a carteira de {} a {}: {}", dataInicio, dataFim, e.getMessage());
            throw new CarteiraErpIndisponivelException(
                    "O ERP não respondeu a tempo ou o cg-scraper está fora do ar. Tente novamente em instantes.");
        } catch (RestClientResponseException e) {
            log.warn("cg-scraper respondeu {} para a carteira de {} a {}: {}", e.getStatusCode(), dataInicio, dataFim,
                    e.getResponseBodyAsString());
            throw new CarteiraErpIndisponivelException(
                    "O ERP devolveu um erro ao montar a carteira. Tente novamente em instantes.");
        }
    }
}
