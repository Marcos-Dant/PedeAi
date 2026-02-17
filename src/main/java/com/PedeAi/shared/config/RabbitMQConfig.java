package com.PedeAi.shared.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Constantes que o PedidoEventPublisher vai usar
    public static final String QUEUE_COZINHA_PEDIDO_PAGO = "cozinha.pedido.pago.queue";
    public static final String QUEUE_PEDIDO_CRIADO = "pedido.criado.queue";
    public static final String EXCHANGE_PEDIDO = "pedido.exchange";
    public static final String ROUTING_KEY_PEDIDO_CRIADO = "pedido.criado";

    public static final String QUEUE_PAGAMENTO_APROVADO = "Pagamento.aprovado.queue";
    public static final String ROUTING_KEY_PAGAMENTO_APROVADO = "pagamento.aprovado";

    public static final String QUEUE_PAGAMENTO_RECUSADO = "pedidos.v1.pagamento-recusado";
    public static final String ROUTING_KEY_PAGAMENTO_RECUSADO = "pedidos.pagamento.recusado";


    @Bean
    public Queue queueCozinhaPedigoPago() {
        return new Queue(QUEUE_COZINHA_PEDIDO_PAGO, true);
    }

    @Bean
    public Queue queuePedidoCriado() {
        return new Queue(QUEUE_PEDIDO_CRIADO, true);
    }

    @Bean
    public Queue queuePagamentoAprovado(){
        return new Queue(QUEUE_PAGAMENTO_APROVADO, true);
    }


    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_PEDIDO);
    }

    @Bean
    public Binding bindingCozinhaPedidoPago(TopicExchange exchange){
        return BindingBuilder.bind(queueCozinhaPedigoPago()).to(exchange).with(ROUTING_KEY_PAGAMENTO_APROVADO);
    }

    @Bean
    public Binding bindingPedidoCriado(TopicExchange exchange) {
        return BindingBuilder.bind(queuePedidoCriado()).to(exchange).with(ROUTING_KEY_PEDIDO_CRIADO);
    }


    @Bean
    public Binding bindingPagamentoAprovado(TopicExchange exchange) {
        return BindingBuilder.bind(queuePagamentoAprovado()).to(exchange).with(ROUTING_KEY_PAGAMENTO_APROVADO);
    }

    @Bean
    public Queue queuePagamentoRecusado() {
        return new Queue(QUEUE_PAGAMENTO_RECUSADO, true);
    }

    @Bean
    public Binding bindingPagamentoRecusado(Queue queuePagamentoRecusado, TopicExchange exchange){
        return BindingBuilder
                .bind(queuePagamentoRecusado)
                .to(exchange)
                .with(ROUTING_KEY_PAGAMENTO_RECUSADO);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}