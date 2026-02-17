package com.PedeAi.catalogo.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_produto")
@Getter @Setter @NoArgsConstructor
public class Produto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    public Produto(String nome, BigDecimal preco, Categoria categoria){
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
        this.ativo = true;
    }


    public void atualizaPreco(BigDecimal novoPreco) {
        if(novoPreco.compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        this.preco = novoPreco;
    }



}
