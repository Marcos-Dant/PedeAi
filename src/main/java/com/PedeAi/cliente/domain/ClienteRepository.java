package com.PedeAi.cliente.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    Page<Cliente> findAllByUsuarioAtivoTrue(Pageable pageable);
    Optional<Cliente> findByUsuarioLogin(String email);
}
