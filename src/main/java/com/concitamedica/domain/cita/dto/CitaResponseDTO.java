package com.concitamedica.domain.cita.dto;

import java.time.LocalDateTime;

public record CitaResponseDTO(
        Long id,
        Long medicoId,
        Long pacienteId,
        String nombreMedico, // Nombre + Apellido
        String nombrePaciente,
        String especialidad,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String estado
) {}