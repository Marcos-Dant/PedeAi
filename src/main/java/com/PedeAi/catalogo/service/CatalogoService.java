/*
package com.PedeAi.catalogo.service;


import com.PedeAi.catalogo.domain.Categoria;
import com.PedeAi.catalogo.domain.CategoriaRepository;
import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;


    public CatalogoService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }


    @Transactional
    public Categoria criarCategoria(Categoria categoria){
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarCategorias(){
        return categoriaRepository.findAll();
    }

    public Categoria buscarCategoriaPorId(Long id){
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontada com id" + id));
    }


    @Transactional
    public Produto criarProduto(Produto produto, Long categoriaId){
        Categoria categoria = buscarCategoriaPorId(categoriaId);

        produto.setCategoria(categoria);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos(){
        return produtoRepository.findAll();
    }

    public List<Produto> listarProdutoPorCategoria(Long categoriaId){
        return produtoRepository.findByCategoriaId(categoriaId);
    }


}

*/
