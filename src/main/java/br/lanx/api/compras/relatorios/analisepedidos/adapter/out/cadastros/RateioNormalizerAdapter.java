package br.lanx.api.compras.relatorios.analisepedidos.adapter.out.cadastros;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.rateios.RateioService;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeRateioPort;

@Component
public class RateioNormalizerAdapter implements NormalizeRateioPort {

    private final RateioService rateioService;

    public RateioNormalizerAdapter(RateioService rateioService) {
        this.rateioService = rateioService;
    }

    @Override
    public UUID obterOuCriar(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        return rateioService.findByNomeOrCreate(nome).getId();
    }
}
