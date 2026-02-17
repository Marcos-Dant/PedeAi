package com.PedeAi.shared;

import com.PedeAi.seguranca.domain.Usuario;
import com.PedeAi.seguranca.domain.UsuarioRepository;
import com.PedeAi.seguranca.domain.UsuarioRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${api.security.admin.password}")
    private String senhaAdmin;

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.findByLogin("admin2@pedeai.com") == null) {

                String senhaCriptografada = passwordEncoder.encode(senhaAdmin);

                Usuario admin = new Usuario();
                admin.setLogin("admin2@pedeai.com");
                admin.setSenha(senhaCriptografada);
                admin.setRole(UsuarioRole.ADMIN);

                repository.save(admin);

                System.out.println("--- ADMIN PADRÃO CRIADO ---");
                System.out.println("Login: admin2@pedeai.com");
                // Mostra a senha no console SÓ para ajudar no desenvolvimento
                System.out.println("Senha: " + senhaAdmin);
                System.out.println("------------------------------");

            }

            if (repository.findByLogin("cliente@pedeai.com") == null) {
                String senhaUser = passwordEncoder.encode("123456");
                Usuario cliente = new Usuario();

                cliente.setLogin("cliente@pedeai.com");
                cliente.setSenha(senhaUser);
                cliente.setRole(UsuarioRole.USER);
                cliente.setAtivo(true);

                repository.save(cliente);

                System.out.println("--- 👤 CLIENTE PADRÃO CRIADO ---");
                System.out.println("Login: cliente@pedeai.com");
                System.out.println("Senha: 123456");
                System.out.println("--------------------------------");
            }
        };
    }
}