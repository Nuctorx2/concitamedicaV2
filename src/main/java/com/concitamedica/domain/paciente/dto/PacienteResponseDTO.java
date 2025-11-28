package com.concitamedica.domain.paciente.dto;

import com.concitamedica.domain.usuario.Usuario;
import java.time.LocalDate;

public record PacienteResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String documento,
        String telefono,
        String direccion,
        LocalDate fechaNacimiento,
        String genero,
        boolean activo
) {
    public PacienteResponseDTO(Usuario usuario) {
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
                usuario.isEnabled()
        );
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public boolean isActivo() { return activo; }
}