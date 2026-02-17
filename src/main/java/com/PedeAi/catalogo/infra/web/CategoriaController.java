package com.PedeAi.catalogo.infra.web;


import com.PedeAi.catalogo.domain.Categoria;
import com.PedeAi.catalogo.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Gerencia as seções do cardápio (Lanches, Bebidas, etc)")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service){
        this.service = service;
    }

    @Secured("ROLE_ADMIN")
    @Operation(summary = "Cadastra uma nova categoria", description = "Cria uma categoria ativa para agrupar produtos")
    @PostMapping
    public ResponseEntity<Categoria> criar(@RequestBody Categoria categoria) {
        categoria.setId(null);
        Categoria novaCategoria = service.criar(categoria);
        return ResponseEntity.ok(novaCategoria);
    }


    @Operation(summary = "Lista todas as categorias", description = "Retorna a listagem completa de categorias disponíveis")
    @GetMapping
    public ResponseEntity<List<Categoria>> listar(){
        return ResponseEntity.ok(service.listarTodas());
    }


    @Secured("ROLE_ADMIN")
    @Operation(summary = "Atualiza uma categoria", description = "Altera o nome ou descrição. O ID na URL define qual categoria será alterada.")
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
        Categoria categoriaAtualizada = service.atualizar(id, categoria);
        return ResponseEntity.ok(categoriaAtualizada);
    }


    @Secured("ROLE_ADMIN")
    @Operation(summary = "Exclui uma categoria", description = "Remove a categoria APENAS se não houver produtos vinculados a ela.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id){
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
