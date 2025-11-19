package com.concitamedica.domain.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PacienteActualizarDTO(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Email String email, // Lo permitiremos, pero validaremos duplicados
        @NotBlank String documento,
        String telefono,
        String direccion,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank String genero
) {}