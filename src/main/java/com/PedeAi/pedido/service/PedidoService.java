package com.PedeAi.pedido.service;


import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import com.PedeAi.cliente.domain.Cliente;
import com.PedeAi.cliente.service.ClienteService;
import com.PedeAi.pedido.domain.ItemPedido;
import com.PedeAi.pedido.domain.Pedido;
import com.PedeAi.pedido.domain.PedidoRepository;
import com.PedeAi.pedido.domain.StatusPedido;
import com.PedeAi.pedido.infra.web.dto.ItemPedidoRequest;
import com.PedeAi.pedido.infra.web.dto.PedidoRequest;
import com.PedeAi.pedido.service.event.PedidoCriadoEvent;
import com.PedeAi.pedido.service.event.PedidoEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteService clienteService;
    private final PedidoEventPublisher eventPublisher;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProdutoRepository produtoRepository,
                         ClienteService clienteService,
                         PedidoEventPublisher eventPublisher){
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.clienteService = clienteService;
        this.eventPublisher = eventPublisher;
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
    }



    @Transactional
    public Pedido criarPedido(PedidoRequest request, String emailCliente) {

        Cliente cliente = clienteService.buscarPorEmail(emailCliente);


        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);


        for (ItemPedidoRequest itemRequest : request.itens()){
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Produto não encontrado: " + itemRequest.produtoId()));

            ItemPedido item = new ItemPedido(produto, itemRequest.quantidade());
            pedido.adicionarItem(item);
        }


        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        eventPublisher.publicarPedidoCriado(new PedidoCriadoEvent(
                pedidoSalvo.getId(),
                pedidoSalvo.getCliente().getId(),
                pedidoSalvo.getTotal()
        ));

        return pedidoSalvo;
    }

    @Transactional
    public Pedido iniciarPreparo(Long id){
        Pedido pedido = buscarPorId(id);
        validarStatus(pedido, StatusPedido.PAGO, "Iniciar o Preparo");

        pedido.setStatus(StatusPedido.EM_PREPARACAO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido marcarComoPronto(Long id) {
        Pedido pedido = buscarPorId(id);
        validarStatus(pedido, StatusPedido.EM_PREPARACAO, "Marcar como pronto");

        pedido.setStatus(StatusPedido.PRONTO);
        return pedidoRepository.save(pedido);
    }


    @Transactional
    public Pedido finalizarPedido(Long id){
        Pedido pedido = buscarPorId(id);
        validarStatus(pedido, StatusPedido.PRONTO, "Finalizar o pedido");

        pedido.setStatus(StatusPedido.FINALIZADO);

        pedido.setDataFinalizacao(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    private void validarStatus(Pedido pedido, StatusPedido statusEsperado, String acao){
        if (pedido.getStatus() != statusEsperado) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Operação Inválida: Para %s, o status deve ser %s. Status atual: %s",
                            acao, statusEsperado, pedido.getStatus()));
        }
    }

    @Transactional
    public void atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        // LÓGICA NOVA
        if (novoStatus == StatusPedido.FINALIZADO) {
            pedido.setDataFinalizacao(LocalDateTime.now());
        } else {
            pedido.setStatus(novoStatus);
            pedido.setDataFinalizacao(null);
        }

        pedido.setStatus(novoStatus);
        pedidoRepository.save(pedido);
    }

    public List<Pedido> listarFilaCozinha(){
        return pedidoRepository.findByStatusIn(List.of(
                StatusPedido.PAGO,
                StatusPedido.EM_PREPARACAO
        ));
    }

    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllComItens();
    }
}
