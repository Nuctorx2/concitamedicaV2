package com.concitamedica.domain.horario.dto;

import com.concitamedica.domain.horario.DiaSemana;
import com.concitamedica.domain.horario.Horario;

import java.time.LocalTime;

/**
 * DTO para exponer la información de un bloque de horario.
 */
public record HorarioResponseDTO(
        Long id,
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin
) {
    /**
     * Método "factory" para convertir una entidad Horario a este DTO.
     * @param horario La entidad a convertir.
     * @return una nueva instancia de HorarioResponseDTO.
     */
    public static HorarioResponseDTO fromEntity(Horario horario) {
        return new HorarioResponseDTO(
                horario.getId(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin()
        );
    }
}