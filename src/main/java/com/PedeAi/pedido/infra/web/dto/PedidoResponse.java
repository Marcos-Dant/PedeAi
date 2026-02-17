package com.PedeAi.pedido.infra.web.dto;

import com.PedeAi.pedido.domain.Pedido;
import com.PedeAi.pedido.domain.StatusPedido;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record PedidoResponse(
        Long id,
        BigDecimal total,
        StatusPedido status,
        Long clienteId,
        Long tempoEsperaMinutos,
        List<ItemPedidoResponse> itens
) {

    public PedidoResponse(Pedido pedido) {
        this(
                pedido.getId(),
                pedido.getTotal(),
                pedido.getStatus(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getTempoEspera(),
                pedido.getItens() != null
                        ? pedido.getItens().stream().map(ItemPedidoResponse::new).toList()
                        : new ArrayList<>()
        );
    }
}
