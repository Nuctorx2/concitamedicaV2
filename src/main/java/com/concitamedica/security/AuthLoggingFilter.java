package com.concitamedica.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class AuthLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println(">>> [AuthLoggingFilter] Usuario en contexto: " + auth.getName());
            System.out.println(">>> [AuthLoggingFilter] Roles: " + auth.getAuthorities());
        } else {
            System.out.println(">>> [AuthLoggingFilter] No hay autenticación en contexto.");
        }
        chain.doFilter(request, response);
    }
}
