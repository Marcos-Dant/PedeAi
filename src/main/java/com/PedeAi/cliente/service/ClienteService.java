package com.PedeAi.cliente.service;


import ch.qos.logback.core.net.server.Client;
import com.PedeAi.cliente.domain.Cliente;
import com.PedeAi.cliente.domain.ClienteRepository;
import com.PedeAi.cliente.infra.web.dto.ClienteRequest;
import com.PedeAi.seguranca.domain.Usuario;
import com.PedeAi.seguranca.domain.UsuarioRepository;
import com.PedeAi.seguranca.domain.UsuarioRole;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository repository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder){
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Cliente cadastrar(ClienteRequest dados) {

        if(repository.existsByCpf(dados.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado!");
        }

        if(usuarioRepository.findByLogin(dados.email()) != null) {
            throw new IllegalArgumentException("Email já cadastrado!");
        }

        String senhaCriptografada = passwordEncoder.encode(dados.senha());

        Usuario usuario = new Usuario();
        usuario.setLogin(dados.email());
        usuario.setSenha(senhaCriptografada);
        usuario.setRole(UsuarioRole.USER);

        usuarioRepository.save(usuario);

        Cliente cliente = new Cliente(
                dados.nome(),
                dados.cpf(),
                dados.telefone(),
                usuario
        );

        return repository.save(cliente);
    }

    public Cliente buscarPorEmail(String email){
        return repository.findByUsuarioLogin(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Cliente não encontrado com o email: " + email));
    }

    public Cliente buscarPorId(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encostando com o ID: " + id));
    }

    // Não devolvemos mais List, devolvemos uma Página
    public Page<Cliente> listarTodos(Pageable pageable) {
        return repository.findAllByUsuarioAtivoTrue(pageable);
    }


    @Transactional
    public void excluir(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        cliente.setNome("Usuário Excluído");
        cliente.setCpf("DEL-" + id);
        cliente.setTelefone(null);
        cliente.getUsuario().setAtivo(false);

        Usuario usuario = cliente.getUsuario();
        usuario.setLogin("excluido_" + id + "@pedeai.com");

        repository.save(cliente);
    }

}
