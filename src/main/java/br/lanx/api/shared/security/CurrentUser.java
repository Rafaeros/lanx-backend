package br.lanx.api.shared.security;

import java.util.UUID;

/** Porta: é isto que os módulos de negócio injetam. Nenhum deles importa Spring Security. */
public interface CurrentUser {
    UUID id();
    String username();
}
