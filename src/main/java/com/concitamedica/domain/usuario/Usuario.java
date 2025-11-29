package com.concitamedica.domain.usuario;

import com.concitamedica.domain.common.Auditable;
import com.concitamedica.domain.rol.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar en blanco.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido no puede estar en blanco.")
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
    private String password;

    @NotNull(message = "La fecha de nacimiento no puede ser nula.")
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El género no puede estar en blanco.")
    @Column(nullable = false, length = 20)
    private String genero;

    @NotNull(message = "El usuario debe tener un rol asignado.")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", referencedColumnName = "id", nullable = false)
    private Rol rol;

    // CAMPO PARA BAJA LÓGICA (Soft Delete)
    // Usamos el valor por defecto 'true' para que los nuevos usuarios nazcan activos
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    // --- MÉTODOS DE USERDETAILS (Obligatorios) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.getNombre()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}