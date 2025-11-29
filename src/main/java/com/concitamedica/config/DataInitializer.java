package com.concitamedica.config;

import com.concitamedica.domain.especialidad.Especialidad;
import com.concitamedica.domain.especialidad.EspecialidadRepository;
import com.concitamedica.domain.horario.DiaSemana;
import com.concitamedica.domain.horario.Horario;
import com.concitamedica.domain.horario.HorarioRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.concitamedica.domain.rol.Roles.*;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final HorarioRepository horarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        crearRoles();
        crearEspecialidades();

        Rol rolAdmin = rolRepository.findByNombre(ROLE_ADMIN).orElseThrow();
        Rol rolMedico = rolRepository.findByNombre(ROLE_MEDICO).orElseThrow();
        Rol rolPaciente = rolRepository.findByNombre(ROLE_PACIENTE).orElseThrow();

        // Admin
        crearUsuarioSiNoExiste("Admin", "Sistema", "99999999", "admin@concitamedica.com", "admin123", rolAdmin);

        // Pacientes
        crearUsuarioSiNoExiste("Sofia", "Navarro", "10000001", "sofia.navarro@email.com", "paciente123", rolPaciente);
        crearUsuarioSiNoExiste("Carlos", "Rojas", "10000002", "carlos.rojas@email.com", "paciente123", rolPaciente);

        // Medicos


        // Medicina General
        crearMedicoConHorario("Juan", "Pérez", "20000001", "juan.perez@email.com", "Medicina General", rolMedico);
        crearMedicoConHorario("Laura", "Gómez", "20000002", "laura.gomez@email.com", "Medicina General", rolMedico);

        // Odontología
        crearMedicoConHorario("Pedro", "Sánchez", "30000001", "pedro.sanchez@email.com", "Odontología", rolMedico);
        crearMedicoConHorario("Lucía", "Díaz", "30000002", "lucia.diaz@email.com", "Odontología", rolMedico);

        // Dermatología
        crearMedicoConHorario("Ana", "Martínez", "40000001", "ana.martinez@email.com", "Dermatología", rolMedico);
        crearMedicoConHorario("Roberto", "Fernández", "40000002", "roberto.fernandez@email.com", "Dermatología", rolMedico);
    }


    private void crearRoles() {
        if (rolRepository.count() == 0) {
            rolRepository.saveAll(List.of(new Rol(ROLE_PACIENTE), new Rol(ROLE_MEDICO), new Rol(ROLE_ADMIN)));
        }
    }

    private void crearEspecialidades() {
        if (especialidadRepository.count() == 0) {
            especialidadRepository.saveAll(List.of(
                    Especialidad.builder().nombre("Medicina General").descripcion("Atención primaria.").build(),
                    Especialidad.builder().nombre("Odontología").descripcion("Cuidado oral.").build(),
                    Especialidad.builder().nombre("Dermatología").descripcion("Cuidado de la piel.").build()
            ));
        }
    }

    private Usuario crearUsuarioSiNoExiste(String nombre, String apellido, String documento, String email, String password, Rol rol) {
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario nuevoUsuario = Usuario.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .documento(documento)
                    .telefono("3110189856")
                    .direccion("Consultorio Central")
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .fechaNacimiento(LocalDate.of(1985, 1, 1))
                    .genero("OTRO")
                    .rol(rol)
                    .build();
            return usuarioRepository.save(nuevoUsuario);
        });
    }


    private void crearMedicoConHorario(String nombre, String apellido, String documento, String email, String especialidadNombre, Rol rolMedico) {

        Usuario usuario = crearUsuarioSiNoExiste(nombre, apellido, documento, email, "medico123", rolMedico);


        if (medicoRepository.findByUsuario(usuario).isEmpty()) {
            Especialidad especialidad = especialidadRepository.findByNombre(especialidadNombre).orElseThrow();

            Medico medico = Medico.builder()
                    .usuario(usuario)
                    .especialidad(especialidad)
                    .build();

            medico = medicoRepository.save(medico);


            asignarHorarioBase(medico);
        }
    }

    private void asignarHorarioBase(Medico medico) {
        List<DiaSemana> diasLaborables = List.of(
                DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES
        );

        for (DiaSemana dia : diasLaborables) {
            if (horarioRepository.findByMedicoIdAndDiaSemana(medico.getId(), dia).isEmpty()) {
                Horario horario = Horario.builder()
                        .medico(medico)
                        .diaSemana(dia)
                        .horaInicio(LocalTime.of(8, 0))
                        .horaFin(LocalTime.of(17, 0))
                        .build();
                horarioRepository.save(horario);
            }
        }
    }
}