package com.concitamedica.domain.especialidad.dto;

import com.concitamedica.domain.especialidad.Especialidad;

public record EspecialidadDTO(
        Long id,
        String nombre
) {
    public static EspecialidadDTO fromEntity(Especialidad especialidad) {
        return new EspecialidadDTO(
                especialidad.getId(),
                especialidad.getNombre()
        );
    }
}