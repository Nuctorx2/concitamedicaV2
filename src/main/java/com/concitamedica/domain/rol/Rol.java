package com.concitamedica.domain.rol;

import jakarta.persistence.*;
import com.concitamedica.domain.common.Auditable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa la tabla 'roles' en la base de datos.
 * Cada instancia de esta clase es un rol que se puede asignar a un usuario.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rol extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String nombre; // Ej: "Paciente", "Medico", "Admin"

    // ✅ Constructor personalizado para crear roles rápidamente
    public Rol(String nombre) {
        this.nombre = nombre;
    }
}
