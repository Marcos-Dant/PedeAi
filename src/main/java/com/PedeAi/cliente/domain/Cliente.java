package com.PedeAi.cliente.domain;

import com.PedeAi.seguranca.domain.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "tb_cliente")
@Getter @Setter @NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String cpf;

    private String telefone;

    private boolean ativo = true;

    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    public Cliente(String nome, String cpf, String telefone, Usuario usuario) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.usuario = usuario;
        this.ativo = true;
    }


}
