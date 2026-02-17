package com.PedeAi.pedido.infra.web.dto;

import com.PedeAi.pedido.domain.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario
) {
    public ItemPedidoResponse(ItemPedido item) {
        this(
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario()
        );
    }
}