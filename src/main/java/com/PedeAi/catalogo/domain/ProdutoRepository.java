package com.PedeAi.catalogo.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("SELECT p FROM Produto p JOIN FETCH p.categoria")
    List<Produto> findAllComCategorias();

    boolean existsByCategoriaId(Long categoriaId);
}
