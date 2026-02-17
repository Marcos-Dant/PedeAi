package com.PedeAi.cliente.infra.web.dto;

import com.PedeAi.cliente.domain.Cliente;

public record ClienteResponse (
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone
) {
    public ClienteResponse(Cliente cliente) {
        this(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getUsuario().getLogin(),
                cliente.getTelefone()
        );
    }
}

