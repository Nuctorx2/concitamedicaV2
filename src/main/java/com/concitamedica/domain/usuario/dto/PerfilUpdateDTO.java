package com.concitamedica.domain.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PerfilUpdateDTO(
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank String documento,
        String telefono,
        String direccion,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank String genero
        // Nota: No incluimos email, password ni rol por seguridad
) {}