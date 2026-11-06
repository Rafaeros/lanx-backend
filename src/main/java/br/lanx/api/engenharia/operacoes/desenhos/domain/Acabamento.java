package br.lanx.api.engenharia.operacoes.desenhos.domain;

/**
 * Acabamento do chicote: a letra da legenda de acabamentos do desenho.
 * Usado nos trechos sem tubo na aba IT TUBOS; o worker recebe só a letra ({@link #name()}).
 */
public enum Acabamento {
    A("Tubo T-Retrátil"),
    B("Tubo FPVC (Vinil) S/ Corte"),
    C("Tubo Corrugado S/ Corte"),
    D("Tubo Corrugado C/ Corte"),
    E("Tubo Corrugado C/ Corte C/ Ponto de Fita"),
    F("Tubo Nylon Flexguard"),
    G("Cabo PP/Manga Multivias"),
    H("Enfitamento Completo C/ Fita Tecido"),
    I("Enfitamento Completo C/ Fita Isolante"),
    J("Cabo Espiralado Multivias"),
    L("Fiobraid 01"),
    M("Fiobraid 02");

    private final String descricao;

    Acabamento(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
