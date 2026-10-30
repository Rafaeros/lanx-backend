package br.lanx.api.producao.shared;

public class ProducaoPermissions {
    public static final String PRODUCAO = "producao";
    public static final String COMUNIZADO = "producao.comunizado";
    public static final String ORDENS_PRODUCAO = "producao.ordens-producao";

    /** Ação extra: importação de dados do ERP (pedidos do comunizado, planilha de OPs). */
    public static final String IMPORTAR = "importar";

    public static final String COMUNIZADO_READ = COMUNIZADO + ":read";
    public static final String COMUNIZADO_IMPORTAR = COMUNIZADO + ":" + IMPORTAR;

    public static final String ORDENS_PRODUCAO_READ = ORDENS_PRODUCAO + ":read";
    public static final String ORDENS_PRODUCAO_CREATE = ORDENS_PRODUCAO + ":create";
    public static final String ORDENS_PRODUCAO_UPDATE = ORDENS_PRODUCAO + ":update";
    public static final String ORDENS_PRODUCAO_DELETE = ORDENS_PRODUCAO + ":delete";
    public static final String ORDENS_PRODUCAO_IMPORTAR = ORDENS_PRODUCAO + ":" + IMPORTAR;

    private ProducaoPermissions() {
    }
}
