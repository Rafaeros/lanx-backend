package br.lanx.api.auth.permission;

import java.util.Locale;

/**
 * Ações que toda funcionalidade tem. Ações extras ("aprovar") vêm do
 * FeatureDefinition.
 */
public enum Action {
    READ, CREATE, UPDATE, DELETE;

    /** "read", "create"... é o sufixo do código da permissão. */
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
