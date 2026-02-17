package com.PedeAi.catalogo.infra.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProdutoRequest (
        String nome,
        String descricao,

        @NotNull @Positive
        BigDecimal preco,

        @NotNull
        Long categoriaId
){}
