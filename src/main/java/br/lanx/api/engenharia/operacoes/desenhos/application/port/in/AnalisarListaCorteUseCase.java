package br.lanx.api.engenharia.operacoes.desenhos.application.port.in;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.AnaliseListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase.Upload;

/** Confere a lista de corte (vias/cavidades dos conectores) sem criar job: só leitura. */
public interface AnalisarListaCorteUseCase {

    AnaliseListaCorte analisar(Upload listaCorte);
}
