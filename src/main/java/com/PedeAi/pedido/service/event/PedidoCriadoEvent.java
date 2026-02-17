package com.PedeAi.pedido.service.event;

import java.io.Serializable;
import java.math.BigDecimal;

public record PedidoCriadoEvent (

    Long idPedido,
    Long idCliente,
    BigDecimal valorTotal

) implements Serializable{}
