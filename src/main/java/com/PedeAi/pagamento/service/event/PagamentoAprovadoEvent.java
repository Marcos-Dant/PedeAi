package com.PedeAi.pagamento.service.event;

import java.io.Serializable;

public record PagamentoAprovadoEvent (Long idPedido) implements Serializable {}
