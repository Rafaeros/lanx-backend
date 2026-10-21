package br.lanx.api.producao.shared;

public class ProducaoPermissions {
    public static final String PRODUCAO = "producao";
    public static final String COMUNIZADO = "producao.comunizado";

    /** Ação extra: dispara a importação dos pedidos a partir do ERP. */
    public static final String IMPORTAR = "importar";

    /** Ação extra: baixa o relatório filtrado em planilha. */
    public static final String EXPORTAR = "exportar";

    public static final String COMUNIZADO_READ = COMUNIZADO + ":read";
    public static final String COMUNIZADO_IMPORTAR = COMUNIZADO + ":" + IMPORTAR;

    private ProducaoPermissions() {
    }
}
