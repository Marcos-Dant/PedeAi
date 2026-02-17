package com.PedeAi.pagamento.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pagamento")
@Getter @Setter @NoArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long pedidoId;


    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    private BigDecimal valor;

    private LocalDateTime dataPagamento;

    public Pagamento(Long pedidoId, BigDecimal valor) {
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = StatusPagamento.PENDENTE;
        this.dataPagamento = LocalDateTime.now();
    }

}
