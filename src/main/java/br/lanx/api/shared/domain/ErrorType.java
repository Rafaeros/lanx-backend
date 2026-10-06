package br.lanx.api.shared.domain;

/** Categoria do erro de negócio. Sem HTTP: é o adaptador web que traduz. */
public enum ErrorType {
    VALIDATION,        // dado de entrada inválido segundo a regra de negócio
    UNAUTHENTICATED,   // não sei quem você é
    FORBIDDEN,         // sei quem você é, mas não pode
    NOT_FOUND,
    CONFLICT,          // ex.: CNPJ já cadastrado
    BUSINESS_RULE      // regra violada: ex.: aprovar pedido já cancelado
}