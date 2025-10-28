package com.concitamedica.config;

import com.concitamedica.domain.especialidad.Especialidad;
import com.concitamedica.domain.especialidad.EspecialidadRepository;
import com.concitamedica.domain.medico.Medico;
import com.concitamedica.domain.medico.MedicoRepository;
import com.concitamedica.domain.rol.Rol;
import com.concitamedica.domain.rol.RolRepository;
import com.concitamedica.domain.usuario.Usuario;
import com.concitamedica.domain.usuario.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import static com.concitamedica.domain.rol.Roles.*;


import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // --- 1. Crear Datos Maestros (Roles y Especialidades) ---
        if (rolRepository.count() == 0) { // Solo si la tabla está vacía
            rolRepository.saveAll(List.of(
                    new Rol(ROLE_PACIENTE),
                    new Rol(ROLE_MEDICO),
                    new Rol(ROLE_ADMIN)
            ));
        }
        if (especialidadRepository.count() == 0) {
            especialidadRepository.saveAll(List.of(
                    new Especialidad("Medicina General"),
                    new Especialidad("Odontología"),
                    new Especialidad("Dermatología")
            ));
        }

        // --- 2. Crear Usuarios de Prueba (si no existen) ---
        // Administrador
        crearUsuarioSiNoExiste("Admin General", "admin@concitamedica.com", "admin123", ROLE_ADMIN);

        // Pacientes
        crearUsuarioSiNoExiste("Sofia Navarro", "sofia.navarro@email.com", "paciente123", ROLE_PACIENTE);
        crearUsuarioSiNoExiste("Carlos Rojas", "carlos.rojas@email.com", "paciente123", ROLE_PACIENTE);

        // Médicos
        Usuario medicoUser1 = crearUsuarioSiNoExiste("Dr. Juan Pérez", "juan.perez@email.com", "medico123", ROLE_MEDICO);
        crearMedicoSiNoExiste(medicoUser1, "Medicina General");

        Usuario medicoUser2 = crearUsuarioSiNoExiste("Dra. Ana Martínez", "ana.martinez@email.com", "medico123", ROLE_MEDICO);
        crearMedicoSiNoExiste(medicoUser2, "Dermatología");
    }

    private Usuario crearUsuarioSiNoExiste(String nombre, String email, String password, String nombreRol) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Rol rol = rolRepository.findByNombre(nombreRol).orElseThrow();
            Usuario nuevoUsuario = Usuario.builder()
                    .nombre(nombre)
                    .email(email)
                    .password(passwordEncoder.encode(password)) // ¡Aquí está la magia!
                    .fechaNacimiento(LocalDate.of(1990, 1, 1))
                    .genero("N/A")
                    .rol(rol)
                    .build();
            return usuarioRepository.save(nuevoUsuario);
        });
    }

    private void crearMedicoSiNoExiste(Usuario usuario, String nombreEspecialidad) {
        // Asumimos que no puede haber un perfil de médico sin un usuario
        if (medicoRepository.findByUsuario(usuario).isEmpty()) {
            Especialidad especialidad = especialidadRepository.findByNombre(nombreEspecialidad).orElseThrow();
            Medico nuevoMedico = Medico.builder()
                    .usuario(usuario)
                    .especialidad(especialidad)
                    .build();
            medicoRepository.save(nuevoMedico);
        }
    }
}