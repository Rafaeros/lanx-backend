package br.lanx.api.cadastros.shared;

/**
 * Chaves e authorities do módulo cadastros. Constantes de compilação: servem em
 * {@code @PreAuthorize("hasAuthority('" + MATERIAIS_READ + "')")}.
 */
public final class CadastrosPermissions {

    public static final String CADASTROS = "cadastros";
    public static final String UNIDADE_MEDIDA = "cadastros.unidade-medida";
    public static final String MATERIAIS = "cadastros.materiais";
    public static final String PAIS = "cadastros.pais";
    public static final String FORNECEDOR = "cadastros.fornecedores";

    public static final String UNIDADE_MEDIDA_READ = UNIDADE_MEDIDA + ":read";
    public static final String UNIDADE_MEDIDA_CREATE = UNIDADE_MEDIDA + ":create";
    public static final String UNIDADE_MEDIDA_UPDATE = UNIDADE_MEDIDA + ":update";
    public static final String UNIDADE_MEDIDA_DELETE = UNIDADE_MEDIDA + ":delete";

    public static final String MATERIAIS_READ = MATERIAIS + ":read";
    public static final String MATERIAIS_CREATE = MATERIAIS + ":create";
    public static final String MATERIAIS_UPDATE = MATERIAIS + ":update";
    public static final String MATERIAIS_DELETE = MATERIAIS + ":delete";

    public static final String PAISES_READ = PAIS + ":read";

    public static final String FORNECEDOR_READ = FORNECEDOR + ":read";
    public static final String FORNECEDOR_CREATE = FORNECEDOR + ":create";
    public static final String FORNECEDOR_UPDATE = FORNECEDOR + ":update";
    public static final String FORNECEDOR_DELETE = FORNECEDOR + ":delete";

    private CadastrosPermissions() {
    }
}
