package br.lanx.api.auth.user.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.lanx.api.auth.user.domain.exception.InvalidUserException;

class PasswordPolicyTest {

    @Test
    @DisplayName("senha com 8+ caracteres, letras e números é aceita")
    void valida() {
        assertThatCode(() -> PasswordPolicy.validate("senha1234")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "abc12", "somenteletras", "123456789" })
    @DisplayName("curta, só letras ou só números é rejeitada")
    void invalida(String senha) {
        assertThatThrownBy(() -> PasswordPolicy.validate(senha)).isInstanceOf(InvalidUserException.class);
    }

    @Test
    @DisplayName("acima de 150 caracteres é rejeitada")
    void longaDemais() {
        assertThatThrownBy(() -> PasswordPolicy.validate("a1".repeat(80)))
                .isInstanceOf(InvalidUserException.class);
    }
}
