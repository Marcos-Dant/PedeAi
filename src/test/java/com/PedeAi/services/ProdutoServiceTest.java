package com.PedeAi.services;


import com.PedeAi.catalogo.domain.Categoria;
import com.PedeAi.catalogo.domain.CategoriaRepository;
import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import com.PedeAi.catalogo.service.ProdutoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {


    @Mock
    private ProdutoRepository repository;

    @Mock
    private CategoriaRepository categoriarepository;

    @InjectMocks
    private ProdutoService service;

    @Test
    @DisplayName("Deve retornar uma lista de produtos com sucesso")
    void deveRetornarListaDeProdutos(){

        Produto produtoFalso = new Produto();
        produtoFalso.setNome("Pizza Teste!");
        produtoFalso.setPreco(BigDecimal.valueOf(50.0));

        Mockito.when(repository.findAllComCategorias()).thenReturn(List.of(produtoFalso));

        List<Produto> resultado = service.listarTodos();

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("Pizza Teste!", resultado.get(0).getNome());

    }

    @Test
    @DisplayName("Deve salvar um produto com sucesso")
    void deveSalvarProduto() {

        Produto produtoParaSalvar = new Produto();
        produtoParaSalvar.setNome("Hamburguer Artesanal");
        produtoParaSalvar.setPreco(BigDecimal.valueOf(35.00));

        Long idCategoria = 99L;
        Categoria categoriaFalsa = new Categoria();
        categoriaFalsa.setId(idCategoria);
        categoriaFalsa.setNome("Lanches");

        Mockito.when(categoriarepository.findById(idCategoria))
                .thenReturn(Optional.of(categoriaFalsa));

        Produto produtoSalvo = new Produto();
        produtoSalvo.setId(1L);
        produtoSalvo.setNome("Hamburguer Artesanal");
        produtoSalvo.setPreco(BigDecimal.valueOf(35.00));
        produtoSalvo.setCategoria(categoriaFalsa);

        Mockito.when(repository.save(Mockito.any(Produto.class))).thenReturn(produtoSalvo);

        Produto resultado = service.criar(produtoParaSalvar, idCategoria);

        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(1L, resultado.getId());
        Assertions.assertEquals("Hamburguer Artesanal", resultado.getNome());

        Mockito.verify(repository, Mockito.times(1)).save(produtoParaSalvar);

    }

}
