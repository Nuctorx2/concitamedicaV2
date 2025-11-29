package com.concitamedica.domain.horario.dto;

import com.concitamedica.domain.horario.DiaSemana;
import com.concitamedica.domain.horario.Horario;

import java.time.LocalTime;

public record HorarioResponseDTO(
        Long id,
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin
) {

    public static HorarioResponseDTO fromEntity(Horario horario) {
        return new HorarioResponseDTO(
                horario.getId(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin()
        );
    }
}