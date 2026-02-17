package com.PedeAi.catalogo.service;

import com.PedeAi.catalogo.domain.Categoria;
import com.PedeAi.catalogo.domain.CategoriaRepository;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import org.springframework.cache.annotation.Cacheable;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final ProdutoRepository produtoRepository;


    public CategoriaService(CategoriaRepository repository, ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    @CacheEvict(value = "categorias", allEntries = true)
    public List<Categoria> listarTodas() {
        System.out.println("CONSULTA LENTA: Buscando categorias no Banco de Dados...");
        return repository.findAll();
    }

    public Optional<Categoria> buscarPorId(Long id){
        return repository.findById(id);
    }

    @Transactional
    @CacheEvict(value = "categorias", allEntries = true)
    public Categoria criar(Categoria categoria){
        System.out.println("Limpando o cache de categorias!");
        return repository.save(categoria);
    }


    @Transactional
    @CacheEvict(value = "categoria", allEntries = true)
    public Categoria atualizar(Long id, Categoria categoriaComNovosDados){
        Categoria categoriaAtual = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

        categoriaAtual.setNome(categoriaComNovosDados.getNome());
        categoriaAtual.setDescricao(categoriaComNovosDados.getDescricao());

        return repository.save(categoriaAtual);

    }


    @Transactional
    @CacheEvict(value = "categoria", allEntries = true)
    public void excluir(Long id) {
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

        boolean temProdutos = produtoRepository.existsByCategoriaId(id);

        if (temProdutos) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir categoria que possui produtos vinculados.");
        }
        repository.delete(categoria);
    }
}
