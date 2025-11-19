package com.concitamedica.domain.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PacienteCreateDTO(
        @NotBlank String nombre,
        String apellido,
        @NotBlank @Email String email,
        String password, // Opcional, se puede generar una por defecto
        String documento,
        String telefono,
        String direccion,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank String genero
) {}