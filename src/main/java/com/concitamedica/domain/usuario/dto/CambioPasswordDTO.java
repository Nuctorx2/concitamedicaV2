package com.concitamedica.domain.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordDTO(
        @NotBlank String passwordActual,

        @NotBlank
        @Size(min = 5, message = "La nueva contraseña debe tener al menos 5 caracteres")
        String nuevaPassword
) {}