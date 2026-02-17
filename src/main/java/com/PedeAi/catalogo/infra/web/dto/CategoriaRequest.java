package com.PedeAi.catalogo.infra.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,
        String descricao
) {}