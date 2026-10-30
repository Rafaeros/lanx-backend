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
    public static final String DEPARTAMENTO = "cadastros.departamentos";
    public static final String OPERADORES = "cadastros.operadores";
    public static final String RATEIOS = "cadastros.rateios";
    public static final String COMPRADORES = "cadastros.compradores";
    public static final String CLIENTES = "cadastros.clientes";
    public static final String TIPOS_NAO_CONFORMIDADE = "cadastros.tipos-nao-conformidade";

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

    public static final String DEPARTAMENTO_READ = DEPARTAMENTO + ":read";
    public static final String DEPARTAMENTO_CREATE = DEPARTAMENTO + ":create";
    public static final String DEPARTAMENTO_UPDATE = DEPARTAMENTO + ":update";
    public static final String DEPARTAMENTO_DELETE = DEPARTAMENTO + ":delete";

    public static final String OPERADORES_READ = OPERADORES + ":read";
    public static final String OPERADORES_CREATE = OPERADORES + ":create";
    public static final String OPERADORES_UPDATE = OPERADORES + ":update";
    public static final String OPERADORES_DELETE = OPERADORES + ":delete";

    public static final String RATEIOS_READ = RATEIOS + ":read";
    public static final String RATEIOS_CREATE = RATEIOS + ":create";
    public static final String RATEIOS_UPDATE = RATEIOS + ":update";
    public static final String RATEIOS_DELETE = RATEIOS + ":delete";

    public static final String COMPRADORES_READ = COMPRADORES + ":read";
    public static final String COMPRADORES_CREATE = COMPRADORES + ":create";
    public static final String COMPRADORES_UPDATE = COMPRADORES + ":update";
    public static final String COMPRADORES_DELETE = COMPRADORES + ":delete";

    public static final String CLIENTES_READ = CLIENTES + ":read";
    public static final String CLIENTES_CREATE = CLIENTES + ":create";
    public static final String CLIENTES_UPDATE = CLIENTES + ":update";
    public static final String CLIENTES_DELETE = CLIENTES + ":delete";

    public static final String TIPOS_NAO_CONFORMIDADE_READ = TIPOS_NAO_CONFORMIDADE + ":read";
    public static final String TIPOS_NAO_CONFORMIDADE_CREATE = TIPOS_NAO_CONFORMIDADE + ":create";
    public static final String TIPOS_NAO_CONFORMIDADE_UPDATE = TIPOS_NAO_CONFORMIDADE + ":update";
    public static final String TIPOS_NAO_CONFORMIDADE_DELETE = TIPOS_NAO_CONFORMIDADE + ":delete";

    private CadastrosPermissions() {
    }
}
