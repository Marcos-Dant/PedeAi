package com.PedeAi.catalogo.infra.web.dto;

import com.PedeAi.catalogo.domain.Categoria;

public record CategoriaResponse(
        Long id,
        String nome,
        String descricao
) {
    public CategoriaResponse(Categoria categoria){
        this(categoria.getId(),
        categoria.getNome(),
        categoria.getDescricao());
    }

}
