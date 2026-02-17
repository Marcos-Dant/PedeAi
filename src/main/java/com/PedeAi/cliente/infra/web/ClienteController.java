package com.PedeAi.cliente.infra.web;


import com.PedeAi.cliente.domain.Cliente;
import com.PedeAi.cliente.infra.web.dto.ClienteRequest;
import com.PedeAi.cliente.infra.web.dto.ClienteResponse;
import com.PedeAi.cliente.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestão de Usuários e Identificação para pedidos")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service){
        this.service = service;
    }



    @Operation(summary = "Cadastra novo cliente", description = "Cria uma conta de usuário com CPF e Email")
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody ClienteRequest request, UriComponentsBuilder uriBuilder, org.springframework.security.core.Authentication authentication){

        if (authentication != null && authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER"))) {
            return org.springframework.http.ResponseEntity.status(403).build();
        }

        Cliente clienteSalvo = service.cadastrar(request);

        ClienteResponse responseDTO = new ClienteResponse(clienteSalvo);

        var uri = uriBuilder.path("/api/clientes/{id}").buildAndExpand(clienteSalvo.getId()).toUri();
        return ResponseEntity.created(uri).body(responseDTO);
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Busca cliente por ID", description = "Recupera os dados detalhados de um cliente específico")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscar(@PathVariable Long id) {
        Cliente cliente = service.buscarPorId(id);
        return ResponseEntity.ok(new ClienteResponse(cliente));
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Lista clientes paginados",
            description = "Retorna uma lista paginada de clientes ativos. Padrão: 10 por página.")
    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> listar(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable){


        Page<Cliente> paginaClientes = service.listarTodos(pageable);

        return ResponseEntity.ok(paginaClientes.map(ClienteResponse::new));
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Exclui (Desativa) um cliente", description = "Realiza a exclusão lógica: anonimiza os dados pessoais e desativa o cadastro. O histórico de pedidos é preservado.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

}
