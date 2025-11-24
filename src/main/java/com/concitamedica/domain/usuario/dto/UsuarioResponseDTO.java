package com.concitamedica.domain.usuario.dto;

import com.concitamedica.domain.usuario.Usuario;
import java.time.LocalDate;

public record UsuarioResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String documento,
        String telefono,
        String direccion,
        LocalDate fechaNacimiento,
        String genero,
        String rol
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getDocumento(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getFechaNacimiento(),
                usuario.getGenero(),
                usuario.getRol().getNombre()
        );
    }
}