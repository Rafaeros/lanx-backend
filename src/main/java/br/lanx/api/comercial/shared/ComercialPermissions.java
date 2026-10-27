package br.lanx.api.comercial.shared;

/**
 * Chaves e authorities do módulo comercial. Constantes de compilação: servem em
 * {@code @PreAuthorize("hasAuthority('" + CARTEIRA_READ + "')")}.
 */
public final class ComercialPermissions {

    public static final String COMERCIAL = "comercial";
    public static final String CARTEIRA = "comercial.carteira";

    /** Ação extra: baixa o relatório filtrado em planilha. */
    public static final String EXPORTAR = "exportar";

    public static final String CARTEIRA_READ = CARTEIRA + ":read";
    public static final String CARTEIRA_EXPORTAR = CARTEIRA + ":" + EXPORTAR;

    private ComercialPermissions() {
    }
}
