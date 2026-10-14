package br.lanx.api.shared.security;

import java.util.Set;
import java.util.UUID;

/**
 * Contrato entre a infraestrutura de segurança e o módulo auth: dado o usuário
 * do token, quais authorities ("compras.analise:read") ele tem agora. Fica em
 * shared para o shared não depender do auth.
 */
public interface AuthorityResolver {
    Set<String> authoritiesOf(UUID userId);
}
