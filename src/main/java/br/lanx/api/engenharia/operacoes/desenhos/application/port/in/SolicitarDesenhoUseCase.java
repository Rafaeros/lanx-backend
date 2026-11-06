package br.lanx.api.engenharia.operacoes.desenhos.application.port.in;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import br.lanx.api.engenharia.operacoes.desenhos.domain.ArquivoEnviado;
import br.lanx.api.engenharia.operacoes.desenhos.domain.DesenhoOpcoes;

/** Cria um pedido de desenho: grava os arquivos, salva o job e manda para a fila do worker. */
public interface SolicitarDesenhoUseCase {

    /** @return o id do job (o front acompanha o status por ele) */
    UUID solicitar(SolicitarDesenhoCommand cmd);

    record SolicitarDesenhoCommand(
            UUID solicitanteId,
            String projeto,
            DesenhoOpcoes opcoes,
            Map<String, Object> projectOverride,
            Upload listaCorte,
            Upload desenhoCliente) {

        public SolicitarDesenhoCommand {
            Objects.requireNonNull(solicitanteId, "solicitanteId");
            // listaCorte ausente não é checada aqui: o domínio recusa com uma mensagem para o usuário (400)
        }
    }

    /** O arquivo sem {@code MultipartFile}: a application não conhece a camada web. */
    record Upload(String nome, String contentType, long tamanho, Supplier<InputStream> conteudo) {
        public ArquivoEnviado descricao() {
            return new ArquivoEnviado(nome, contentType, tamanho);
        }
    }
}
