package com.PedeAi.seguranca.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MonitorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Só verifica se for uma requisição de mudança de status (PATCH)
        if (request.getMethod().equals("PATCH") && request.getRequestURI().startsWith("/api/pedidos")) {

            String token = request.getHeader("X-Monitor-Token");

            // Se o token não vier ou for errado, bloqueia!
            if (!"SEGREDO_DA_COZINHA_123".equals(token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado: Token inválido");
                return;
            }
        }

        // Se passar, continua a vida
        filterChain.doFilter(request, response);
    }
}