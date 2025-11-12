package com.concitamedica.domain.cita.dto;

import java.time.LocalDateTime;

public record CitaMedicoResponseDTO(
        Long citaId,
        String nombrePaciente,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String estado
) {}
