package br.lanx.api.producao.operacoes.comunizado.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DesponteTest {

    @Test
    void umaMedidaComUmaCasa() {
        assertThat(Desponte.normalizar("6")).isEqualTo("6.0");
        assertThat(Desponte.normalizar("6.0")).isEqualTo("6.0");
        assertThat(Desponte.normalizar("3,5")).isEqualTo("3.5");
        assertThat(Desponte.normalizar("16.00")).isEqualTo("16.0");
    }

    @Test
    void duasMedidasSeparadasPorBarra() {
        assertThat(Desponte.normalizar("40.0/6.0")).isEqualTo("40.0/6.0");
        assertThat(Desponte.normalizar("40 / 3,5")).isEqualTo("40.0/3.5");
    }

    @Test
    void vazioViraNulo() {
        assertThat(Desponte.normalizar(null)).isNull();
        assertThat(Desponte.normalizar("  ")).isNull();
    }

    @Test
    void textoQueNaoEMedidaEInvalido() {
        assertThatThrownBy(() -> Desponte.normalizar("40/abc")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Desponte.normalizar("40/")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Desponte.normalizar("-4.0")).isInstanceOf(IllegalArgumentException.class);
    }
}
