package br.lanx.api.producao.operacoes.comunizado;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Size;

/**
 * Campos do formulário (multipart) além do arquivo da IT. Todos opcionais:
 * completam o que a IT não traz (operador, OP, quantidade, início, fim) ou
 * sobrescrevem o que ela traz (produto, docReferencia).
 */
public record ComunizadoRequest(
        @Size(max = 60) String produto,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate docReferencia,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate realizadoEm,
        @Size(max = 30) String quantidade,
        @Size(max = 40) String op,
        @Size(max = 40) String inicio,
        @Size(max = 40) String fim,
        @Size(max = 80) String operador) {
}
