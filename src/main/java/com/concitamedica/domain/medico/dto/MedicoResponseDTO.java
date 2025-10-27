package com.concitamedica.domain.medico.dto;

/**
 * DTO para exponer la información pública de un Médico.
 * Es una "vista" segura de la entidad Medico, sin exponer datos sensibles como contraseñas.
 */
public record MedicoResponseDTO(
        Long id,          // El ID de la entidad Medico
        String nombre,
        String email,
        String especialidadNombre
) {}