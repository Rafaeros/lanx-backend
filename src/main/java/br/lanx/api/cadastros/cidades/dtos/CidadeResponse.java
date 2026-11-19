package br.lanx.api.cadastros.cidades.dtos;

import br.lanx.api.cadastros.cidades.Cidade;

public record CidadeResponse (
    Integer codigoIbge,
    String nome,
    String uf
){
    public static CidadeResponse fromEntity(Cidade c){
        return new CidadeResponse(c.getCodigoIbge(), c.getNome(), c.getEstado().getUf());
    }
}
