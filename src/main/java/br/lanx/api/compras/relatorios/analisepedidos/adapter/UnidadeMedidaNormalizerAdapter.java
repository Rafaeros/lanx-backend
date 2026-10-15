package br.lanx.api.compras.relatorios.analisepedidos.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.lanx.api.cadastros.unidademedida.application.port.in.FindOrCreateUnidadeMedidaUseCase;
import br.lanx.api.compras.relatorios.analisepedidos.application.port.out.NormalizeUnidadeMedidaPort;

@Component
public class UnidadeMedidaNormalizerAdapter implements NormalizeUnidadeMedidaPort {

    // Itens importados sem unidade de medida caem nesta unidade, já que Material exige uma
    static final String SIGLA_NAO_INFORMADA = "N/I";
    static final String NOME_NAO_INFORMADA = "Não informada";

    private final FindOrCreateUnidadeMedidaUseCase findOrCreateUnidadeMedidaUseCase;

    public UnidadeMedidaNormalizerAdapter(FindOrCreateUnidadeMedidaUseCase findOrCreateUnidadeMedidaUseCase) {
        this.findOrCreateUnidadeMedidaUseCase = findOrCreateUnidadeMedidaUseCase;
    }

    @Override
    public UUID obterOuCriar(String sigla, String nome) {
        String normalizedSigla = sigla == null || sigla.isBlank() ? null : sigla.trim();
        String normalizedNome = nome == null || nome.isBlank() ? null : nome.trim();

        if (normalizedSigla == null && normalizedNome == null) {
            normalizedSigla = SIGLA_NAO_INFORMADA;
            normalizedNome = NOME_NAO_INFORMADA;
        }

        String effectiveSigla = normalizedSigla != null ? normalizedSigla : normalizedNome;
        String effectiveName = normalizedNome != null ? normalizedNome : normalizedSigla;

        var command = new FindOrCreateUnidadeMedidaUseCase.Command(effectiveName, effectiveSigla);
        return findOrCreateUnidadeMedidaUseCase.findByNomeOrCreate(command).unidade().id();
    }

}
