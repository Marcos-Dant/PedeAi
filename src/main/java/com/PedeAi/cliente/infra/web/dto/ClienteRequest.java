package com.PedeAi.cliente.infra.web.dto;

public record  ClienteRequest(
        String nome,
        String cpf,
        String email,
        String senha,
        String telefone
) {}
