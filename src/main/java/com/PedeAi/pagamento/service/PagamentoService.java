package com.PedeAi.pagamento.service;


import com.PedeAi.pagamento.domain.Pagamento;
import com.PedeAi.pagamento.domain.PagamentoRepository;
import com.PedeAi.pagamento.domain.StatusPagamento;
import com.PedeAi.pagamento.service.event.PagamentoAprovadoEvent;
import com.PedeAi.pedido.service.event.PedidoCriadoEvent;
import com.PedeAi.shared.config.RabbitMQConfig;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PagamentoService {

    private final PagamentoRepository repository;
    private final RabbitTemplate rabbitTemplate;


    public PagamentoService(PagamentoRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }
//apenas para meus testes de pagamento vou implementar uma API externa
    public void iniciarPagamento(Long pedidoId, BigDecimal valor) {

        // 1. BLINDAGEM 1: Verificar antes de fazer (Check)
        if (repository.existsByPedidoId(pedidoId)) {
            System.out.println("⚠️ Pagamento já existe para o pedido " + pedidoId + ". Ignorando duplicidade.");
            return;
        }

        System.out.println("Processando pagamento para o pedido " + pedidoId + " no valor de R$ " + valor);

        try {
            Pagamento pagamento = new Pagamento(pedidoId, valor);
            pagamento.setStatus(StatusPagamento.APROVADO);

            // 2. AÇÃO: Tentar Salvar
            repository.save(pagamento);

            // 3. Notificar (Só notifica se salvou com sucesso)
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PEDIDO,
                    RabbitMQConfig.ROUTING_KEY_PAGAMENTO_APROVADO,
                    new PagamentoAprovadoEvent(pedidoId)
            );

            System.out.println("✅ Pagamento APROVADO e evento enviado para o pedido " + pedidoId);

        } catch (DataIntegrityViolationException e) {
            // 4. BLINDAGEM 2: Rede de Segurança (Handle)
            // Se chegou aqui, é porque duas threads tentaram salvar ao mesmo tempo.
            // Engolimos o erro para o RabbitMQ não entrar em loop.
            System.out.println("🛑 Erro de concorrência capturado: O pagamento já foi criado por outro processo.");
        }
    }


}
