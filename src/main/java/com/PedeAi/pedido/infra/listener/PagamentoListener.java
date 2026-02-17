package com.PedeAi.pedido.infra.listener;


import com.PedeAi.pagamento.service.event.PagamentoAprovadoEvent;
import com.PedeAi.pagamento.service.event.PagamentoRecusadoEvent;
import com.PedeAi.pedido.domain.Pedido;
import com.PedeAi.pedido.domain.PedidoRepository;
import com.PedeAi.pedido.domain.StatusPedido;
import com.PedeAi.shared.config.RabbitMQConfig;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PagamentoListener {

    private final PedidoRepository repository;

    public PagamentoListener(PedidoRepository repository){
        this.repository = repository;
    }


    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAGAMENTO_APROVADO)
    @Transactional
    public void aoReceberConfirmacaoPagamento(PagamentoAprovadoEvent evento){
        System.out.println("Recebi confirmação de pagamento para o Pedido " + evento.idPedido());

        Pedido pedido = repository.findById(evento.idPedido())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado ao processar pagamento via RabbitMQ"));

        pedido.setStatus(StatusPedido.PAGO);
        repository.save(pedido);

        System.out.println("Status do Pedido" + evento.idPedido() + " atualizado para PAGO no banco!");
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAGAMENTO_RECUSADO)
    @Transactional
    public void aoReceberRecusaPagamento(PagamentoRecusadoEvent evento){
        System.out.println("Pagamento recusado para o pedido: " + evento.idPedido());
        System.out.println("Movito: " + evento.motivo());

        Pedido pedido = repository.findById(evento.idPedido())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado ao processar via RabbitMQ"));
        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);

        System.out.println("Status do Pedido " + evento.idPedido() + "atualizado para CANCELADO no banco!");
    }


}
