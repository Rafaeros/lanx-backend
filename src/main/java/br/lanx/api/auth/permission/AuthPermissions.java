package br.lanx.api.auth.permission;

/**
 * Chaves e authorities do módulo auth. São constantes de compilação, então
 * servem em {@code @PreAuthorize("hasAuthority('" + USUARIOS_READ + "')")}:
 * erro de digitação vira erro de build, não 403 em produção.
 */
public final class AuthPermissions {

    public static final String ADMINISTRACAO = "administracao";
    public static final String USUARIOS = "administracao.usuarios";
    public static final String PERFIS = "administracao.permissoes";

    public static final String USUARIOS_READ = USUARIOS + ":read";
    public static final String USUARIOS_CREATE = USUARIOS + ":create";
    public static final String USUARIOS_UPDATE = USUARIOS + ":update";

    public static final String PERFIS_READ = PERFIS + ":read";
    public static final String PERFIS_CREATE = PERFIS + ":create";
    public static final String PERFIS_UPDATE = PERFIS + ":update";

    private AuthPermissions() {
    }
}
