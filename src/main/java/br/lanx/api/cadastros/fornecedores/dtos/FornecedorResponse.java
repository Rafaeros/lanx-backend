package br.lanx.api.cadastros.fornecedores.dtos;

import java.util.UUID;

import br.lanx.api.cadastros.fornecedores.Fornecedor;
import br.lanx.api.cadastros.pais.Pais;

public record FornecedorResponse(
        UUID id,
        String nome,
        /** Null quando o fornecedor não tem país. */
        PaisSummary pais) {

    public record PaisSummary(UUID id, String nome, String codigoIso2) {
    }

    public static FornecedorResponse fromEntity(Fornecedor fornecedor) {
        Pais pais = fornecedor.getPais();
        return new FornecedorResponse(fornecedor.getId(), fornecedor.getNome(),
                pais == null ? null : new PaisSummary(pais.getId(), pais.getNome(), pais.getCodigoIso2()));
    }
}
