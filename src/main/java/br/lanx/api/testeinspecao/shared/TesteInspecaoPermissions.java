package br.lanx.api.testeinspecao.shared;

/**
 * Chaves e authorities do módulo teste e inspeção. Constantes de compilação:
 * servem em {@code @PreAuthorize("hasAuthority('" + NAOCONFORMIDADES_READ + "')")}.
 */
public final class TesteInspecaoPermissions {

    public static final String TESTE_INSPECAO = "teste-inspecao";
    public static final String NAOCONFORMIDADES = "teste-inspecao.nao-conformidade";

    /** Ação extra: baixa a listagem filtrada em planilha. */
    public static final String EXPORTAR = "exportar";

    public static final String NAOCONFORMIDADES_CREATE = NAOCONFORMIDADES + ":create";
    public static final String NAOCONFORMIDADES_READ = NAOCONFORMIDADES + ":read";
    public static final String NAOCONFORMIDADES_UPDATE = NAOCONFORMIDADES + ":update";
    public static final String NAOCONFORMIDADES_DELETE = NAOCONFORMIDADES + ":delete";
    public static final String NAOCONFORMIDADES_EXPORTAR = NAOCONFORMIDADES + ":" + EXPORTAR;

    private TesteInspecaoPermissions() {
    }
}
