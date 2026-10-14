package br.lanx.api.cadastros.materiais.dtos;

public record MaterialFilter(
        String codigo,
        String descricao) {
    public boolean hasCodigo() {
        return codigo != null && !codigo.isBlank();
    }

    public boolean hasDescricao() {
        return descricao != null && !descricao.isBlank();
    }
}