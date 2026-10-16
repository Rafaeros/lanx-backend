package br.lanx.api.compras.shared;

/**
 * Chaves e authorities do módulo compras. Constantes de compilação: servem em
 * {@code @PreAuthorize("hasAuthority('" + ANALISE_PEDIDOS_READ + "')")}.
 */
public final class ComprasPermissions {

    public static final String COMPRAS = "compras";
    public static final String ANALISE_PEDIDOS = "compras.analise-pedidos";

    /** Ação extra: dispara a importação dos pedidos a partir do ERP. */
    public static final String IMPORTAR = "importar";

    public static final String ANALISE_PEDIDOS_READ = ANALISE_PEDIDOS + ":read";
    public static final String ANALISE_PEDIDOS_IMPORTAR = ANALISE_PEDIDOS + ":" + IMPORTAR;

    private ComprasPermissions() {
    }
}
