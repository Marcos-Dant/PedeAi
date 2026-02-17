package com.PedeAi.seguranca.service;


import com.PedeAi.seguranca.domain.Usuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario){
        try {
            return Jwts.builder()
                    .issuer("API PedeAi")
                    .subject(usuario.getLogin())
                    .claim("role", usuario.getRole())
                    .expiration(dataExpiracao())
                    .signWith(getChaveSecreta())
                    .compact();
        }  catch (Exception exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            return Jwts.parser()
                    .verifyWith(getChaveSecreta())
                    .build()
                    .parseSignedClaims(tokenJWT)
                    .getPayload()
                    .getSubject();
        } catch (Exception exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }
    private Date dataExpiracao() {
        return Date.from(LocalDateTime.now().plusHours(2).atZone(ZoneId.of("-03:00")).toInstant());
    }

    private SecretKey getChaveSecreta() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}