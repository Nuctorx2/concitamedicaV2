package com.concitamedica.domain.medico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar la información de un médico.
 * Solo contiene los campos que se permite modificar.
 */
public record ActualizacionMedicoDTO(
        @NotBlank String nombre,
        @NotNull Long especialidadId
) {}