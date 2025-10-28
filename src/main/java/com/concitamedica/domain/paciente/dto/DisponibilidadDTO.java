package com.concitamedica.domain.paciente.dto;

import java.time.LocalTime;

public record DisponibilidadDTO(
        LocalTime horaInicio
) {}