package com.PedeAi.catalogo.service;

import com.PedeAi.catalogo.domain.Categoria;
import com.PedeAi.catalogo.domain.CategoriaRepository;
import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private final ProdutoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }


    @Cacheable(value = "produtos")
    public List<Produto> listarTodos(){
        System.out.println("CONSULTA LENTA: Indo buscar produtos no Banco de Dados MySQL...");
        return repository.findAllComCategorias();
    }

    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    @CacheEvict(value = "produtos", allEntries = true)
    public Produto criar(Produto produto, Long categoriaId) {
        System.out.println("Limpando o cache de produtos para manter atualizado!");

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com Id: " + categoriaId));

        produto.setCategoria(categoria);

        return repository.save(produto);

    }

    @Transactional
    @CacheEvict(value = "produtos", allEntries = true)
    public Produto atualizar(Long id, Produto produtoAtualizado){
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setNome(produtoAtualizado.getNome());
        produto.setPreco(produtoAtualizado.getPreco());
        produto.setCategoria(produtoAtualizado.getCategoria());

        return repository.save(produto);

    }

    @Transactional
    @CacheEvict(value = "produtos", allEntries = true)
    public void excluir(Long id) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        produto.setAtivo(false);
        repository.save(produto);
    }
}
