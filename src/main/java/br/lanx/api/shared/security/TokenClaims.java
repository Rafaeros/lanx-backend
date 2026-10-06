package br.lanx.api.shared.security;

/** Nomes das claims. Quem emite (auth) e quem lê (converter) usam as mesmas constantes. */
public final class TokenClaims {
    public static final String USERNAME = "username";
    public static final String PERMISSIONS = "perms";
    private TokenClaims() {}
}