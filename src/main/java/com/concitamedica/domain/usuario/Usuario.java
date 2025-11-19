package com.concitamedica.domain.usuario;

import com.concitamedica.domain.common.Auditable;
import com.concitamedica.domain.rol.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad que representa la tabla 'usuarios' en la base de datos.
 * Contiene la información de login y los datos personales de todos los usuarios del sistema.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Patrón de diseño para construir objetos de forma más legible.
public class Usuario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar en blanco.") // Validación: no nulo y no vacío.
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido no puede estar en blanco.") // Validación: no nulo y no vacío.
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String apellido;


    @NotBlank(message = "El número de documento no puede estar vacío")
    @Size(min = 8, max = 10, message = "El número de documento debe tener entre 8 y 10 caracteres.")
    @Column(nullable = false, length = 10)
    private String documento;

    private String telefono;

    private String direccion;

    @NotBlank(message = "El email no puede estar en blanco.")
    @Email(message = "El formato del email no es válido.")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "La contraseña no puede estar en blanco.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    @Column(name = "password_hash", nullable = false)
    private String password; // Se almacenará el hash, no la contraseña en texto plano.

    @NotNull(message = "La fecha de nacimiento no puede ser nula.")
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El género no puede estar en blanco.")
    @Column(nullable = false, length = 20)
    private String genero;

    // --- Relaciones con otras entidades ---

    @NotNull(message = "El usuario debe tener un rol asignado.")
    @ManyToOne(fetch = FetchType.EAGER) // Indica una relación de muchos a uno (muchos usuarios pueden tener un rol).
    @JoinColumn(name = "rol_id", referencedColumnName = "id", nullable = false) // Define la columna de la clave foránea.
    private Rol rol;
}