package com.PedeAi.cozinha;


import com.PedeAi.pagamento.service.event.PagamentoAprovadoEvent;
import com.PedeAi.pedido.domain.PedidoRepository;
import com.PedeAi.shared.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class CozinhaListener {

    private final PedidoRepository repository;

    public CozinhaListener(PedidoRepository repository){
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COZINHA_PEDIDO_PAGO)
    public void monitorarPedidosPagos(PagamentoAprovadoEvent evento){
        System.out.println("Cozinha: Novo pedido pago (ID " + evento.idPedido() + " Chegou no painel. Aguardando Inicio do preparo.");
    }

}
