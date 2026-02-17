package com.PedeAi.pagamento.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    boolean existsByPedidoId(Long pedidoId);

    Optional<Pagamento> findByPedidoId(Long pedidoId);
}