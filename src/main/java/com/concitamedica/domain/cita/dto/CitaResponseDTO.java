package com.concitamedica.domain.cita.dto;

import java.time.LocalDateTime;

public record CitaResponseDTO(
        Long id,
        Long medicoId,
        String nombreMedico,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String estado
) {}
