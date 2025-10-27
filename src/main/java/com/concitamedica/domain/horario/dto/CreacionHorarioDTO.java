package com.concitamedica.domain.horario.dto;

import com.concitamedica.domain.horario.DiaSemana;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * DTO para la creación de un nuevo bloque de horario para un médico.
 */
public record CreacionHorarioDTO(
        @NotNull DiaSemana diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {}