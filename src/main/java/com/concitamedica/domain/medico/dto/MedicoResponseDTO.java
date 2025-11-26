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
) {}