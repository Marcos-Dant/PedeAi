package com.PedeAi;

import com.PedeAi.catalogo.domain.Categoria;
import com.PedeAi.catalogo.domain.CategoriaRepository;
import com.PedeAi.catalogo.domain.Produto;
import com.PedeAi.catalogo.domain.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // ✅ IMPORT CORRIGIDO
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class CategoriaIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProdutoRepository produtoRepository;


    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");


    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.10-management"));


    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void limparBanco(){
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar uma categoria com sucesso(PUT)")
    void deveAtualizarCategoria() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNome("Bebidas");
        categoria.setDescricao("Geral");
        categoria = categoriaRepository.save(categoria);

        String jsonUpdate = """
                {
                    "nome": "Bebidas Geladas",
                    "descricao": "Sucos e refris"
                }
                """;

        mockMvc.perform(put("/api/categorias/" + categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUpdate)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Bebidas Geladas"));

        Categoria atualizada = categoriaRepository.findById(categoria.getId()).orElseThrow();
        assertEquals("Bebidas Geladas", atualizada.getNome());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve excluir categoria vazia (DELETE)")
    void deveExcluirCategoriaVazia() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Hambuguer", "Carne de bufalo", "Com produtos"));

        mockMvc.perform(delete("/api/categorias/" + categoria.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertFalse(categoriaRepository.existsById(categoria.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Não deve excluir categoria que possui produtos(Erro 409)")
    void naoDeveExcluirCategoriaComProdutos() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Hambuguer", "Carne de bufalo", "Com produtos"));

        Produto produto = new Produto();
        produto.setNome("X-Burguer");
        produto.setPreco(BigDecimal.TEN);
        produto.setCategoria(categoria);
        produto.setAtivo(true);
        produtoRepository.save(produto);

        mockMvc.perform(delete("/api/categorias/" + categoria.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isConflict());

        assertTrue(categoriaRepository.existsById(categoria.getId()));
    }
}