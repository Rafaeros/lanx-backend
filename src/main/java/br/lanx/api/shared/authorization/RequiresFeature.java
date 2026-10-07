package br.lanx.api.shared.authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca o controller (ou o método) como pertencente a uma funcionalidade.
 * A ação exigida sai do verbo HTTP: GET → READ, POST → CREATE,
 * PUT/PATCH → UPDATE, DELETE → DELETE.
 * Sem esta anotação a rota exige apenas autenticação.
 * A chave precisa existir no catálogo, senão a aplicação nem sobe.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RequiresFeature {
    /** Chave da funcionalidade, ex.: "compras.analise". */
    String value();
}
