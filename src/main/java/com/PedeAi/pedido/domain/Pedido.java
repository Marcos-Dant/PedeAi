package com.PedeAi.pedido.domain;


import com.PedeAi.cliente.domain.Cliente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_pedido")
@Getter @Setter @NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataFinalizacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private StatusPedido status = StatusPedido.PENDENTE;

    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens = new ArrayList<>();

    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
        calcularTotal();
    }

    private void calcularTotal() {
        this.total = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getTempoEspera() {
        if (this.dataFinalizacao != null) {
            return java.time.Duration.between(this.dataCriacao, this.dataFinalizacao).toMinutes();
        }
        return java.time.Duration.between(this.dataCriacao, LocalDateTime.now()).toMinutes();
    }

    public void finalizarPedido() {
        this.status = StatusPedido.FINALIZADO;
        this.dataFinalizacao = LocalDateTime.now();
    }



}
