package com.concitamedica.domain.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para el registro de un nuevo usuario.
 * Contiene solo los campos necesarios y las validaciones para el proceso de registro.
 */
public record RegistroUsuarioDTO(
        @NotBlank(message = "El nombre no puede estar en blanco.")
        String nombre,

        @NotBlank(message = "El email no puede estar en blanco.")
        @Email(message = "El formato del email no es válido.")
        String email,

        @NotBlank(message = "La contraseña no puede estar en blanco.")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
        String password,

        @NotNull(message = "La fecha de nacimiento no puede ser nula.")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El género no puede estar en blanco.")
        String genero
) {}