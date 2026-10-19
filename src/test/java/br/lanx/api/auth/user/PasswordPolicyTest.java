package br.lanx.api.auth.user;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.lanx.api.auth.user.exception.InvalidUserException;

class PasswordPolicyTest {

    @Test
    void aceitaSenhaForte() {
        assertThatCode(() -> PasswordPolicy.validateStrong("Compras#2026x", "maria")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Curt#1a         | pelo menos 10",
            "semmaiuscula#12 | maiúscula",
            "SEMMINUSCULA#12 | minúscula",
            "SemNumero#abc   | número",
            "SemSimbolo2026  | símbolo",
            "Com Espaco#2026 | espaços",
            "Maria#Senha2026 | nome de usuário" })
    void recusaSenhaFraca(String senha, String motivo) {
        assertThatThrownBy(() -> PasswordPolicy.validateStrong(senha, "maria"))
                .isInstanceOf(InvalidUserException.class)
                .hasMessageContaining(motivo);
    }

    @Test
    void recusaAcimaDoLimiteDoBcrypt() {
        assertThatThrownBy(() -> PasswordPolicy.validateStrong("Aa1#" + "x".repeat(70), "maria"))
                .hasMessageContaining("longa demais");
    }
}
