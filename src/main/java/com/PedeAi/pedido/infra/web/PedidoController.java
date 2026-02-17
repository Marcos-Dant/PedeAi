package com.PedeAi.pedido.infra.web;


import com.PedeAi.pedido.domain.Pedido;
import com.PedeAi.pedido.infra.web.dto.PedidoRequest;
import com.PedeAi.pedido.infra.web.dto.PedidoResponse;
import com.PedeAi.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos e fluxo de cozinha")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service){
        this.service = service;

    }


    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPedido(@PathVariable Long id){
        Pedido pedido = service.buscarPorId(id);
        return ResponseEntity.ok(new PedidoResponse(pedido));
    }

    @GetMapping("/cozinha")
    @Secured("ROLE_ADMIN")
    @Operation(summary = "Lista fila de cozinha", description = "Retorna pedidos PAGOS ou EM_PREPARACAO")
    public ResponseEntity<List<PedidoResponse>> listarPedidosCozinha() {
        var pedidos = service.listarFilaCozinha();
        var resposta = pedidos.stream().map(PedidoResponse::new).toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping
    @Operation(summary = "Lista todos os pedidos", description = "Retorna a lista completa de pedidos")
    public ResponseEntity<List<PedidoResponse>> listarTodos(){
        List<Pedido> pedidos = service.listarTodos();

        List<PedidoResponse> resposta = pedidos.stream()
                .map(PedidoResponse::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }


    @GetMapping("/{id}/status")
    @Operation(summary = "Consulta status e tempo", description = "Retorna status atual e tempo de espera")
    public ResponseEntity<Map<String, Object>> consultarStatus(@PathVariable Long id) {
        Pedido pedido = service.buscarPorId(id);

        long minutosEspera = 0;
        if (pedido.getDataCriacao() != null) {
            minutosEspera = Duration.between(pedido.getDataCriacao(), LocalDateTime.now()).toMinutes();
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("pedidoId", pedido.getId());
        resposta.put("status", pedido.getStatus());
        resposta.put("tempoEsperaMinutos", minutosEspera);
        resposta.put("itensQtd", pedido.getItens().size());

        return ResponseEntity.ok(resposta);
    }


    @Operation(summary = "Cria um novo pedido", description = "Registra um pedido para o cliente e dispara eventos assíncronos") // <--- E isso
    @PostMapping
    @Secured("ROLE_USER")
    public ResponseEntity<PedidoResponse> criar(@RequestBody PedidoRequest request, Authentication authentication) {
        String emailCliente = authentication.getName();

        Pedido pedido = service.criarPedido(request, emailCliente);

        return ResponseEntity.ok(new PedidoResponse(pedido));
    }


    @PatchMapping("/{id}/preparar")
    @Operation(summary = "Muda status para EM_PREPARACAO")
    public ResponseEntity<PedidoResponse> iniciarPreparo(@PathVariable Long id) {
        Pedido pedido = service.iniciarPreparo(id);
        return ResponseEntity.ok(new PedidoResponse(pedido));
    }

    @PatchMapping("/{id}/pronto")
    @Operation(summary = "Muda status para PRONTO")
    public ResponseEntity<PedidoResponse> marcarComoPronto(@PathVariable Long id) {
        Pedido pedido = service.marcarComoPronto(id);
        return ResponseEntity.ok(new PedidoResponse(pedido));
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Muda status para FINALIZADO")
    public ResponseEntity<PedidoResponse> finalizar(@PathVariable Long id){
        Pedido pedido = service.finalizarPedido(id);
        return ResponseEntity.ok(new PedidoResponse(pedido));
    }


}
