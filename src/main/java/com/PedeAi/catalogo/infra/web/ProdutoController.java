package com.PedeAi.catalogo.infra.web;


import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.infra.web.dto.ProdutoRequest;
import com.PedeAi.catalogo.infra.web.dto.ProdutoResponse;
import com.PedeAi.catalogo.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Administração do Catálogo e Itens do Cardápio")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service){
        this.service = service;
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Cadastra novo produto", description = "Adiciona um item ao cardápio vinculado a uma categoria")
    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@RequestBody @Valid ProdutoRequest request){

        Produto novoProduto = new Produto(request.nome(), request.preco(), null);

        Produto produtoSalvo = service.criar(novoProduto, request.categoriaId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ProdutoResponse(produtoSalvo));
    }


    @Operation(summary = "Listar produtos", description = "Lista todos os produtos existentes")
    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listar(){
        var response = service.listarTodos().stream()
                .map(ProdutoResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }


    @Secured("ROLE_ADMIN")
    @Operation(summary = "Atualiza produto", description = "Permite alterar preço, nome ou descrição de um produto existente")
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produto) {
        Produto produtoAtualizado = service.atualizar(id, produto);

        return ResponseEntity.ok(produtoAtualizado);
    }


    @Secured("ROLE_ADMIN")
    @Operation(summary = "Desativa um produto", description = "Marca o produto como inativo para não aparecer mais no cardápio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

}

