package com.PedeAi;

import com.PedeAi.catalogo.domain.Categoria;
import com.PedeAi.catalogo.domain.CategoriaRepository;
import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import com.PedeAi.cliente.domain.Cliente;
import com.PedeAi.cliente.domain.ClienteRepository;
import com.PedeAi.pagamento.service.event.PagamentoAprovadoEvent;
import com.PedeAi.pagamento.service.event.PagamentoRecusadoEvent;
import com.PedeAi.pedido.domain.Pedido;
import com.PedeAi.pedido.domain.PedidoRepository;
import com.PedeAi.pedido.domain.StatusPedido;
import com.PedeAi.pedido.infra.web.dto.ItemPedidoRequest;
import com.PedeAi.pedido.infra.web.dto.PedidoRequest;
import com.PedeAi.seguranca.domain.Usuario;
import com.PedeAi.seguranca.domain.UsuarioRepository;
import com.PedeAi.seguranca.domain.UsuarioRole;
import com.PedeAi.shared.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.docker.compose.enabled=false") // Desativa Docker Compose para evitar conflito
@AutoConfigureMockMvc
@Testcontainers
public class PedidoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.10-management"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    private Long produtoId;
    private Long clienteId;
    private final String emailCliente = "marcos.teste@pedeai.com";

    @BeforeEach
    void setup(){
        pedidoRepository.deleteAll();
        clienteRepository.deleteAll();
        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();
        categoriaRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setLogin(emailCliente);
        usuario.setSenha("123456");
        usuario.setRole(UsuarioRole.USER);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente("Marcos", "111.111.111-11", "9999-9999", usuario);
        cliente = clienteRepository.save(cliente);
        this.clienteId = cliente.getId();

        Categoria categoria = new Categoria("Lanches", "Melhores Lanches", "Com produtos");
        categoria = categoriaRepository.save(categoria);

        Produto produto = produtoRepository.save(new Produto("Burguer Test", new BigDecimal("30.00"), categoria));
        produto.setAtivo(true);
        produto = produtoRepository.save(produto);
        produtoId = produto.getId();
    }

    @Test
    @DisplayName("Fluxo E2E: Criar -> Evento Pagamento (Rabbit) -> Processar -> Cozinha")
    void fluxoCompletoComInfraReal() throws Exception {

        ItemPedidoRequest item = new ItemPedidoRequest(produtoId, 1);
        PedidoRequest request = new PedidoRequest(List.of(item));

        //FAZ O POST SIMULANDO USUÁRIO LOGADO
        String jsonResponse = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(emailCliente).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();

        PedidoResponse pedidoResposta = objectMapper.readValue(jsonResponse, PedidoResponse.class);
        Long idDinamico = pedidoResposta.id();

        PagamentoAprovadoEvent evento = new PagamentoAprovadoEvent(idDinamico);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PEDIDO,
                RabbitMQConfig.ROUTING_KEY_PAGAMENTO_APROVADO,
                evento
        );

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(200))
                .until(() -> {
                    // Recarrega do banco para ver se mudou status
                    return pedidoRepository.findById(idDinamico)
                            .map(p -> p.getStatus() == StatusPedido.PAGO)
                            .orElse(false);
                });


        mockMvc.perform(patch("/api/pedidos/" + idDinamico + "/preparar")
                        .header("X-Monitor-Token", "SEGREDO_DA_COZINHA_123")
                        .with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARACAO"));

        mockMvc.perform(patch("/api/pedidos/" + idDinamico + "/pronto")
                        .header("X-Monitor-Token", "SEGREDO_DA_COZINHA_123")
                        .with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"));

        mockMvc.perform(patch("/api/pedidos/" + idDinamico + "/finalizar")
                        .header("X-Monitor-Token", "SEGREDO_DA_COZINHA_123")
                        .with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADO"));
    }

    @Test
    @DisplayName("Fluxo Alternativo: Pagamento Recusado -> Pedido Cancelado")
    void deveCancelarPedidoQuandoPagamentoRecusado() throws Exception {
        ItemPedidoRequest item = new ItemPedidoRequest(produtoId, 1);
        PedidoRequest request = new PedidoRequest(List.of(item));

        String responseJson = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(emailCliente).roles("USER")))
                .andReturn().getResponse().getContentAsString();


        PedidoResponse pedidoResposta = objectMapper.readValue(responseJson, PedidoResponse.class);
        Long idDinamico = pedidoResposta.id();

        PagamentoRecusadoEvent eventoRecusa = new PagamentoRecusadoEvent(idDinamico, "Saldo Insuficiente");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PEDIDO,
                RabbitMQConfig.ROUTING_KEY_PAGAMENTO_RECUSADO,
                eventoRecusa
        );

        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    Pedido p = pedidoRepository.findById(idDinamico).orElseThrow();
                    return p.getStatus() == StatusPedido.CANCELADO;
                });
    }

    //Classe auxiliar interna para deserializar a resposta do JSON apenas com o ID
    record PedidoResponseDTO(Long id, String status) {}

    @Test
    @DisplayName("Erro: Tentar preparar pedido que ainda está PENDENTE (Sem pagamento)")
    void deveBloquearPreparoDePedidoNaoPago() throws Exception {
        Pedido pedido = new Pedido();
        Cliente clienteRef = clienteRepository.findById(clienteId).orElseThrow();
        pedido.setCliente(clienteRef);
        pedido.setTotal(new BigDecimal("30.00"));
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido = pedidoRepository.save(pedido);

        Long idDinamico = pedido.getId();

        mockMvc.perform(patch("/api/pedidos/" + idDinamico + "/preparar")
                        .with(user("admin").roles("ADMIN")))
                .andDo(print())
                .andExpect(status().isForbidden());

        Pedido pedidoNoBanco = pedidoRepository.findById(idDinamico).orElseThrow();
        if (pedidoNoBanco.getStatus() != StatusPedido.PENDENTE){
            throw new RuntimeException("O status mudou indevidamente!");
        }
    }
    record PedidoResponse(Long id, String status) {}
}