package com.concitamedica.domain.usuario.dto;

import com.concitamedica.domain.usuario.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String email,
        String rol
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre() // Ejemplo: "ROLE_PACIENTE"
        );
    }
}