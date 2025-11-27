package com.concitamedica.domain.usuario.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PerfilUpdateDTO(

        @NotBlank(message = "El nombre no puede estar en blanco.")
        @Size(min = 2, max = 40, message = "El nombre debe tener entre 2 y 40 caracteres.")
        @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿÑñÁÉÍÓÚáéíóúÜü'\\-\\s]{2,40}$",
                message = "El nombre solo puede contener letras, espacios, acentos, guiones o apóstrofes.")
        String nombre,

        @NotBlank(message = "El apellido no puede estar en blanco.")
        @Size(min = 2, max = 40, message = "El apellido debe tener entre 2 y 40 caracteres.")
        @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿÑñÁÉÍÓÚáéíóúÜü'\\-\\s]{2,40}$",
                message = "El apellido solo puede contener letras, espacios, acentos, guiones o apóstrofes.")
        String apellido,

        @NotBlank(message = "El documento no puede estar en blanco.")
        @Pattern(regexp = "^\\d{8,10}$",
                message = "El documento debe contener solo números y tener entre 8 y 10 dígitos.")
        String documento,

        @Pattern(regexp = "^\\d{7,15}$",
                message = "El teléfono debe contener solo números y tener entre 7 y 15 dígitos.")
        String telefono,

        @Size(max = 100, message = "La dirección no puede tener más de 100 caracteres.")
        String direccion,

        @NotNull(message = "La fecha de nacimiento no puede ser nula.")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El género no puede estar en blanco.")
        @Pattern(
                regexp = "^(MASCULINO|FEMENINO|OTRO)$",
                message = "El género debe ser MASCULINO, FEMENINO u OTRO."
        )
        String genero
) {}
