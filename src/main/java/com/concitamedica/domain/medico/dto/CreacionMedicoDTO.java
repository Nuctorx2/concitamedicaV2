package com.concitamedica.domain.medico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO para la creación de un nuevo perfil de Médico.
 * Contiene tanto los datos para la entidad Usuario como para la entidad Medico.
 */
public record CreacionMedicoDTO(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank String genero,
        @NotNull Long especialidadId // El ID de la especialidad a la que pertenecerá.
) {}