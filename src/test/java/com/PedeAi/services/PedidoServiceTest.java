package com.PedeAi.services;

import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import com.PedeAi.cliente.domain.Cliente;
import com.PedeAi.cliente.service.ClienteService;
import com.PedeAi.pedido.domain.Pedido;
import com.PedeAi.pedido.domain.PedidoRepository;
import com.PedeAi.pedido.infra.web.dto.ItemPedidoRequest;
import com.PedeAi.pedido.infra.web.dto.PedidoRequest;
import com.PedeAi.pedido.service.PedidoService;
import com.PedeAi.pedido.service.event.PedidoEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    @DisplayName("Deve criar o pedido com sucesso e calcular o total corretamente")
    void deveCriarPedidoComSucesso(){

        String emailCliente = "teste@pedeai.com";
        Long produtoId = 10L;
        BigDecimal precoProduto = new BigDecimal("50.00");
        int quantidade = 2;

        ItemPedidoRequest itemRequest = new ItemPedidoRequest(produtoId, quantidade);
        PedidoRequest request = new PedidoRequest(List.of(itemRequest));

        //MOCk DO CLIENTE
        com.PedeAi.cliente.domain.Cliente clienteFalso = new com.PedeAi.cliente.domain.Cliente();
        clienteFalso.setId(1L);
        clienteFalso.setNome("Cliente Teste");

        when(clienteService.buscarPorEmail(emailCliente))
                .thenReturn(clienteFalso);

        //MOck de Produto
        Produto produtoFalso = new Produto();
        produtoFalso.setId(produtoId);
        produtoFalso.setPreco(precoProduto);

        when(produtoRepository.findById(produtoId))
                .thenReturn(Optional.of(produtoFalso));

        //Mock de salvamento
        when(pedidoRepository.save(Mockito.any(Pedido.class)))
                .thenAnswer(invocation -> {
                    Pedido p = invocation.getArgument(0);
                    p.setId(100L);
                    return p;
                });


        Pedido resultado = pedidoService.criarPedido(request, emailCliente);

        //Verificações
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(100L, resultado.getId());

        BigDecimal totalEsperado = new BigDecimal("100.00");
        Assertions.assertEquals(totalEsperado, resultado.getTotal());

        // Verifica se o evento foi publicado
        verify(eventPublisher, times(1))
                .publicarPedidoCriado(any());

        // Verifica se chamou o serviço de cliente pelo email correto
        verify(clienteService, times(1))
                .buscarPorEmail(emailCliente);
    }

    @Test
    @DisplayName("Deve falhar ao tentar criar pedido com produto inexistente")
    void deveFalharQuandoProdutoNaoExiste(){


        String emailCliente = "teste@pedeai.com";
        Long produtoInexistenteId = 999L;


        ItemPedidoRequest itemRequest = new ItemPedidoRequest(produtoInexistenteId, 1);
        PedidoRequest request = new PedidoRequest(List.of(itemRequest));

        Cliente clienteFalso = new Cliente();
        when(clienteService.buscarPorEmail(emailCliente)).thenReturn(clienteFalso);


        when(produtoRepository.findById(produtoInexistenteId))
                .thenReturn(Optional.empty());


        ResponseStatusException erro = Assertions.assertThrows(ResponseStatusException.class, () -> {
            pedidoService.criarPedido(request, emailCliente);
        });

        Assertions.assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        Assertions.assertEquals("Produto não encontrado: " + produtoInexistenteId, erro.getReason());

        verify(pedidoRepository, never()).save(Mockito.any());

    }


}
