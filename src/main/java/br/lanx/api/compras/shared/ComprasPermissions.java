package br.lanx.api.compras.shared;

/**
 * Chaves e authorities do módulo compras. Constantes de compilação: servem em
 * {@code @PreAuthorize("hasAuthority('" + ANALISE_PEDIDOS_READ + "')")}.
 */
public final class ComprasPermissions {

    public static final String COMPRAS = "compras";
    public static final String ANALISE_PEDIDOS = "compras.analise-pedidos";
    public static final String IQF = "compras.iqf";

    /** Ação extra: dispara a importação dos pedidos a partir do ERP. */
    public static final String IMPORTAR = "importar";

    /** Ação extra: baixa o relatório filtrado em planilha. */
    public static final String EXPORTAR = "exportar";

    public static final String ANALISE_PEDIDOS_READ = ANALISE_PEDIDOS + ":read";
    public static final String ANALISE_PEDIDOS_IMPORTAR = ANALISE_PEDIDOS + ":" + IMPORTAR;
    public static final String ANALISE_PEDIDOS_EXPORTAR = ANALISE_PEDIDOS + ":" + EXPORTAR;
    public static final String IQF_READ = IQF + ":read";
    public static final String IQF_EXPORTAR = IQF + ":" + EXPORTAR;

    private ComprasPermissions() {
    }
}
