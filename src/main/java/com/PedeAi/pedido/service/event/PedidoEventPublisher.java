package com.PedeAi.pedido.service.event;

import com.PedeAi.shared.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarPedidoCriado(PedidoCriadoEvent evento) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PEDIDO,
                RabbitMQConfig.ROUTING_KEY_PEDIDO_CRIADO,
                evento
        );
        System.out.println("Evento publicado: Pedido #" + evento.idPedido());
    }
}