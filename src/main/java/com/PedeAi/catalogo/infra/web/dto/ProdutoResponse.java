package com.PedeAi.catalogo.infra.web.dto;

import com.PedeAi.catalogo.domain.Produto;

import java.math.BigDecimal;

public record ProdutoResponse (
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        String nomeCategoria
) {
    public ProdutoResponse(Produto produto) {
        this(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria().getNome()
        );
    }
}




