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
) {
    public CitaResponseDTO {}

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public String getNombrePaciente() { return nombrePaciente; }
    public String getNombreMedico() { return nombreMedico; }
    public String getEspecialidad() { return especialidad; }
    public String getEstado() { return estado; }
}