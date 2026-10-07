package br.lanx.api.shared.authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige uma ação extra nomeada (ex.: "aprovar", "anexar-arquivo") da
 * funcionalidade indicada por {@link RequiresFeature}. Quando presente, ela
 * SUBSTITUI a checagem CRUD pelo verbo HTTP: a ação extra sozinha basta.
 * A ação precisa estar declarada na FeatureDefinition da funcionalidade.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresAction {
    String value();
}
