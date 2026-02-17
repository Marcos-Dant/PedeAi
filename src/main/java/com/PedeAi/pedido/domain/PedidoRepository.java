package com.PedeAi.pedido.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByStatusIn(List<StatusPedido> status);

    List<Pedido> findByClienteId(Long clienteId);


    @Query("SELECT DISTINCT p FROM Pedido p " +
            "LEFT JOIN FETCH p.itens i " +
            "LEFT JOIN FETCH i.produto " +
            "ORDER BY p.id DESC")
    List<Pedido> findAllComItens();

}
