package br.lanx.api.engenharia.operacoes.desenhos.application.dto;

import java.util.List;

import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.ProblemaListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.ProblemaListaCorte.Nivel;

/** Resultado da conferência da lista de corte, mostrado ao usuário antes de enviar para gerar. */
public record AnaliseListaCorte(int cabos, int conectores, int conectoresConferidos,
        List<ProblemaListaCorte> problemas) {

    public boolean temErro() {
        return problemas.stream().anyMatch(p -> p.nivel() == Nivel.ERRO);
    }
}
