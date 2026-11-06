package br.lanx.api.engenharia.operacoes.desenhos.application.port.out;

import java.io.InputStream;
import java.util.List;

import br.lanx.api.engenharia.operacoes.desenhos.domain.listacorte.CaboListaCorte;

/** Lê a aba de cabos da lista de corte (.xlsx). Planilha ilegível → {@code DesenhoInvalidoException}. */
public interface LeitorListaCortePort {

    List<CaboListaCorte> lerCabos(InputStream xlsx);
}
