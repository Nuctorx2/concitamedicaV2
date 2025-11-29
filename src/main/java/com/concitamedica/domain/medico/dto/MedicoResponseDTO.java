package com.concitamedica.domain.medico.dto;

import java.time.LocalDate;

public record MedicoResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String documento,
        String telefono,
        String direccion,
        LocalDate fechaNacimiento,
        String genero,
        String especialidadNombre,
        Long especialidadId,
        boolean activo
) {
    public MedicoResponseDTO { }

    // Getters compatibles con Jasperreports

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getEspecialidadNombre() { return especialidadNombre; }
}