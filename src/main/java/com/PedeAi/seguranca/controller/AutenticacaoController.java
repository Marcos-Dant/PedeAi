package com.PedeAi.seguranca.controller;


import com.PedeAi.seguranca.domain.Usuario;
import com.PedeAi.seguranca.domain.UsuarioRepository;
import com.PedeAi.seguranca.domain.UsuarioRole;
import com.PedeAi.seguranca.dto.DadosAutenticacao;
import com.PedeAi.seguranca.dto.DadosTokenJWT;
import com.PedeAi.seguranca.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados){

        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());

        var authentication = manager.authenticate(authenticationToken);

        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
    }


    /*@PostMapping("/registrar")
    public ResponseEntity registrar(@RequestBody @Valid DadosAutenticacao dados){
        if (repository.findByLogin(dados.login()) != null) return ResponseEntity.badRequest().build();


        String senhaCriptografada = passwordEncoder.encode(dados.senha());
        Usuario novoUsuario = new Usuario(null, dados.login(), senhaCriptografada, UsuarioRole.ADMIN);

        repository.save(novoUsuario);

        return ResponseEntity.ok().build();
    }*/


}
