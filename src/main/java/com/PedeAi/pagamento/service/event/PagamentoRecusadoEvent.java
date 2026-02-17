package com.PedeAi.pagamento.service.event;

import java.io.Serializable;

public record PagamentoRecusadoEvent(Long idPedido, String motivo) implements Serializable {}