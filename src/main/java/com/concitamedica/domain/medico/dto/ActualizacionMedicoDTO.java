package com.concitamedica.domain.medico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ActualizacionMedicoDTO(
        @NotBlank String nombre,
        @NotBlank String apellido, // 🆕
        @NotBlank String documento, // 🆕
        String telefono, // 🆕
        String direccion, // 🆕
        @NotNull LocalDate fechaNacimiento, // 🆕
        @NotBlank String genero, // 🆕
        @NotNull Long especialidadId
) {}