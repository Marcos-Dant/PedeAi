package com.PedeAi.pagamento.infra.listener;


import com.PedeAi.pagamento.service.PagamentoService;
import com.PedeAi.pedido.service.event.PedidoCriadoEvent;
import com.PedeAi.shared.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoCriadoListener {

    private final PagamentoService pagamentoService;

    public PagamentoCriadoListener(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PEDIDO_CRIADO)
    public void aoReceberPedidoCriado(PedidoCriadoEvent evento) {
        System.out.println("Listener de pagamento recebeu evento do pedido: " + evento.idPedido());


        pagamentoService.iniciarPagamento(
                evento.idPedido(),
                evento.valorTotal()
        );

    }

}
