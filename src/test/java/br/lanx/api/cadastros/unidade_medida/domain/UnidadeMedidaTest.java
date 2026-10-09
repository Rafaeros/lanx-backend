package br.lanx.api.cadastros.unidade_medida.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnidadeMedidaTest {

    @Test
    @DisplayName("create normaliza nome/sigla e aplica padrão fator 1")
    void createValido() {
        // Arrange (Prepara) & Act (Age)
        UnidadeMedida unidade = UnidadeMedida.create("  Caixa ", "  CX  ", null);

        // Assert (Verificação)
        assertThat(unidade.nome()).isEqualTo("caixa");
        assertThat(unidade.sigla()).isEqualTo("CX");
        assertThat(unidade.fatorConversao()).isEqualTo(BigDecimal.ONE);
        assertThat(unidade.id()).isNotNull();
    }

    @Test
    @DisplayName("nome ou sigla vazios devem lançar exceção")
    void nomeSiglaInvalidos() {
        // 1. Força o erro do Nome (passando nome null e sigla certa)
        assertThatThrownBy(() -> UnidadeMedida.create(null, "CX", BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O nome é obrigatório");
        // 2. Força o erro da Sigla (passando nome certo e sigla null/vazia)
        assertThatThrownBy(() -> UnidadeMedida.create("Caixa", "   ", BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A sigla é obrigatória");
    }

    @Test
    @DisplayName("fator de conversão zero ou menor que zero deve lançar exceção")
    void fatorConversaoInvalido() {
        assertThatThrownBy(() -> UnidadeMedida.create("Caixa", "CX", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O fator de conversão deve ser maior que zero");
        assertThatThrownBy(() -> UnidadeMedida.create("Caixa", "CX", new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O fator de conversão deve ser maior que zero");
    }

}
