package com.concitamedica.domain.horario.dto;

import com.concitamedica.domain.horario.DiaSemana;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record CreacionHorarioDTO(
        @NotNull DiaSemana diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {}