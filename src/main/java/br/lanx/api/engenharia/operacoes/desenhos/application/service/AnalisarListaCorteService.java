package br.lanx.api.engenharia.operacoes.desenhos.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import br.lanx.api.engenharia.operacoes.desenhos.application.dto.AnaliseListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.AnalisarListaCorteUseCase;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.in.SolicitarDesenhoUseCase.Upload;
import br.lanx.api.engenharia.operacoes.desenhos.application.port.out.LeitorListaCortePort;
import br.lanx.api.engenharia.operacoes.desenhos.domain.exceptions.DesenhoInvalidoException;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.AnaliseConectores;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.CaboListaCorte;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.CodigoConector;
import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.PontaConector;

@Service
class AnalisarListaCorteService implements AnalisarListaCorteUseCase {

    private final LeitorListaCortePort leitor;

    AnalisarListaCorteService(LeitorListaCortePort leitor) {
        this.leitor = leitor;
    }

    @Override
    public AnaliseListaCorte analisar(Upload listaCorte) {
        if (listaCorte == null)
            throw new DesenhoInvalidoException("Envie a lista de corte (.xlsx).");
        if (listaCorte.nome() == null || !listaCorte.nome().toLowerCase().endsWith(".xlsx"))
            throw new DesenhoInvalidoException("A lista de corte precisa ser um arquivo .xlsx.");
        List<CaboListaCorte> cabos;
        try (InputStream in = listaCorte.conteudo().get()) {
            cabos = leitor.lerCabos(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<PontaConector> pontas = cabos.stream().flatMap(c -> Stream.of(c.a(), c.b()))
                .filter(Objects::nonNull).filter(p -> !p.ref().isEmpty()).toList();
        int conectores = (int) pontas.stream().map(PontaConector::ref).distinct().count();
        int conferidos = (int) pontas.stream().filter(p -> CodigoConector.vias(p.codigo()).isPresent())
                .map(PontaConector::ref).distinct().count();
        return new AnaliseListaCorte(cabos.size(), conectores, conferidos, AnaliseConectores.analisar(cabos));
    }
}
