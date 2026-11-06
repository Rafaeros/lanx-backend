package br.lanx.api.engenharia.operacoes.desenhos.adapter.in.web;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Parte "request" (JSON) do POST multipart. Os arquivos vêm nas partes "listaCorte" e "desenhoCliente".
 *
 * @param formatos        "a4" e/ou "painel"; vazio = só "a4"
 * @param acabamento      letra da legenda (A–J, L, M) ou {@code null} para usar a aba IT TUBOS
 * @param projectOverride campos do project.json revisados (carimbo, topologia); opcional
 */
public record SolicitarDesenhoRequest(
        @NotBlank(message = "O código do projeto é obrigatório")
        @Pattern(regexp = "^[\\w.\\- ]{1,100}$", message = "Use letras, números, espaço, \".\", \"-\" ou \"_\"")
        String projeto,
        List<String> formatos,
        @Pattern(regexp = "^[A-JLM]$", message = "Acabamento inválido (use A–J, L ou M)") String acabamento,
        Boolean vistas,
        Boolean biblioteca,
        Boolean dwg,
        Map<String, Object> projectOverride) {
}
