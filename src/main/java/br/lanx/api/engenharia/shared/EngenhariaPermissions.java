package br.lanx.api.engenharia.shared;

/**
 * Chaves e authorities do módulo engenharia. Constantes de compilação: servem em
 * {@code @PreAuthorize("hasAuthority('" + DESENHOS_READ + "')")}.
 */
public final class EngenhariaPermissions {

    public static final String ENGENHARIA = "engenharia";
    public static final String DESENHOS = "engenharia.desenhos";

    /** Ação extra: gera de novo um desenho já concluído ou com erro. */
    public static final String REPROCESSAR = "reprocessar";

    /** Ver o histórico e baixar os arquivos. */
    public static final String DESENHOS_READ = DESENHOS + ":read";
    /** Enviar uma lista de corte para gerar os desenhos. */
    public static final String DESENHOS_CREATE = DESENHOS + ":create";
    public static final String DESENHOS_REPROCESSAR = DESENHOS + ":" + REPROCESSAR;

    private EngenhariaPermissions() {
    }
}
