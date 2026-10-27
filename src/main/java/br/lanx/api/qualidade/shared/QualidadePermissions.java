package br.lanx.api.qualidade.shared;

/**
 * Chaves e authorities do módulo qualidade. Constantes de compilação: servem em
 * {@code @PreAuthorize("hasAuthority('" + IQF_READ + "')")}.
 */
public final class QualidadePermissions {

    public static final String QUALIDADE = "qualidade";
    public static final String IQF = "qualidade.iqf";

    /** Ação extra: baixa o relatório filtrado em planilha. */
    public static final String EXPORTAR = "exportar";

    public static final String IQF_READ = IQF + ":read";
    public static final String IQF_EXPORTAR = IQF + ":" + EXPORTAR;

    private QualidadePermissions() {
    }
}
