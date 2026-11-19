package br.lanx.api.rh.shared;

public class RecursosHumanosPermissions {

    public static final String COLABORADORES = "colaboradores";

    public static final String COLABORADORES_READ = COLABORADORES + ":read";
    public static final String COLABORADORES_CREATE = COLABORADORES + ":create";
    public static final String COLABORADORES_UPDATE = COLABORADORES + ":update";
    public static final String COLABORADORES_DELETE = COLABORADORES + ":delete";
    public static final String COLABORADORES_IMPORTAR = COLABORADORES + ":importar";
    public static final String COLABORADORES_EXPORTAR = COLABORADORES + ":exportar";

    private RecursosHumanosPermissions() {
    }
}
