package br.lanx.api.engenharia.operacoes.desenhos.application.event;

import java.util.List;

public record DesenhoExcluidoEvent(List<String> chaves) {
}
