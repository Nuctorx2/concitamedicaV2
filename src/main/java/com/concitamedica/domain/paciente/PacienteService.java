package com.concitamedica.domain.paciente;

import com.concitamedica.domain.cita.Cita;
import com.concitamedica.domain.cita.CitaRepository;
import com.concitamedica.domain.cita.dto.CitaResponseDTO;
import com.concitamedica.domain.horario.DiaSemana;
import com.concitamedica.domain.horario.Horario;
import com.concitamedica.domain.horario.HorarioRepository;
import com.concitamedica.domain.paciente.dto.*;
import com.concitamedica.domain.rol.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.concitamedica.domain.cita.EstadoCita;
import com.concitamedica.domain.usuario.Usuario;
import com.concitamedica.domain.usuario.UsuarioRepository;
import com.concitamedica.domain.medico.Medico;

import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.concitamedica.domain.medico.MedicoRepository;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final MedicoRepository medicoRepository; // Lo necesitarás, así que asegúrate de inyectarlo
    private final HorarioRepository horarioRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<DisponibilidadDTO> calcularDisponibilidad(Long medicoId, LocalDate fecha) {
        // 1. Verificar si el médico existe
        if (!medicoRepository.existsById(medicoId)) {
            throw new RuntimeException("Médico no encontrado");
        }

        // 2. Encontrar el horario de trabajo del médico para ese día de la semana
        DiaSemana dia = DiaSemana.from(fecha);
        Optional<Horario> horarioLaboralOpt = horarioRepository.findByMedicoIdAndDiaSemana(medicoId, dia);

        if (horarioLaboralOpt.isEmpty()) {
            return new ArrayList<>(); // El médico no trabaja ese día, devuelve lista vacía
        }
        Horario horarioLaboral = horarioLaboralOpt.get();

        // 3. Obtener todas las citas ya agendadas para ese médico en esa fecha
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);
        List<Cita> citasAgendadas = citaRepository.findAllByMedicoIdAndFechaHoraInicioBetween(medicoId, inicioDelDia, finDelDia);
        List<LocalTime> horasOcupadas = citasAgendadas.stream()
                .map(cita -> cita.getFechaHoraInicio().toLocalTime())
                .toList();

        // 4. Generar todos los slots posibles y filtrar los ocupados
        List<DisponibilidadDTO> slotsDisponibles = new ArrayList<>();
        LocalTime horaActual = horarioLaboral.getHoraInicio();

        while (horaActual.isBefore(horarioLaboral.getHoraFin())) {
            if (!horasOcupadas.contains(horaActual)) {
                slotsDisponibles.add(new DisponibilidadDTO(horaActual));
            }
            horaActual = horaActual.plusMinutes(30); // Avanzamos al siguiente slot
        }

        return slotsDisponibles;
    }

    /**
     * Agenda una nueva cita para un paciente.
     * @param datosAgendamiento DTO con los detalles de la cita.
     * @param emailPaciente El email del paciente autenticado (extraído del token).
     * @return La entidad Cita recién creada.
     */
    @Transactional
    public CitaResponseDTO agendarCita(AgendarCitaDTO datosAgendamiento, String emailPaciente) {
        // 1. Obtener las entidades
        Usuario paciente = usuarioRepository.findByEmail(emailPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(datosAgendamiento.medicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        LocalDateTime fechaInicioNueva = datosAgendamiento.fechaHoraInicio();
        LocalDateTime fechaFinNueva = fechaInicioNueva.plusMinutes(30); // Asumimos duración estándar

        // --- 🛡️ REGLAS DE NEGOCIO ---

        // 0. Regla de Tiempo: No agendar en el pasado (Validación defensiva)
        if (fechaInicioNueva.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("No se pueden agendar citas en el pasado.");
        }

        // Preparamos los rangos del día para consultar UNA sola vez
        LocalDateTime inicioDia = fechaInicioNueva.toLocalDate().atStartOfDay();
        LocalDateTime finDia = fechaInicioNueva.toLocalDate().atTime(LocalTime.MAX);

        // Traemos TODAS las citas del paciente en ese día (Optimizamos DB calls)
        List<Cita> citasDelDia = citaRepository.findAllByPacienteIdAndFechaHoraInicioBetween(
                paciente.getId(), inicioDia, finDia
        );

        // Iteramos una sola vez sobre las citas del día para validar MÚLTIPLES reglas
        for (Cita citaExistente : citasDelDia) {

            // 1. Regla de Ubicuidad (Mejorada: Detectar Superposición de horarios)
            // Si la nueva cita empieza antes de que termine la actual Y la nueva termina después de que empiece la actual
            boolean seSuperpone = fechaInicioNueva.isBefore(citaExistente.getFechaHoraFin()) &&
                    fechaFinNueva.isAfter(citaExistente.getFechaHoraInicio());

            if (seSuperpone) {
                throw new IllegalStateException("Conflicto de horario: Ya tienes una cita agendada ("
                        + citaExistente.getMedico().getUsuario().getNombre() + " - "
                        + citaExistente.getFechaHoraInicio().toLocalTime() + ") que se cruza con este horario.");
            }

            // 2. Regla de Especialidad Diaria
            if (citaExistente.getMedico().getEspecialidad().getId().equals(medico.getEspecialidad().getId())) {
                throw new IllegalStateException("Restricción diaria: Ya tienes una cita de "
                        + medico.getEspecialidad().getNombre()
                        + " agendada para este día. Solo se permite una cita por especialidad al día.");
            }
        }

        // 3. Validar disponibilidad del médico (Que el slot exista y esté libre en SU agenda)
        List<DisponibilidadDTO> disponibilidad = calcularDisponibilidad(medico.getId(), fechaInicioNueva.toLocalDate());

        boolean slotDisponible = disponibilidad.stream()
                .anyMatch(slot -> slot.horaInicio().equals(fechaInicioNueva.toLocalTime()));

        if (!slotDisponible) {
            throw new IllegalStateException("El horario seleccionado ya no está disponible en la agenda del médico.");
        }

        // 4. Crear y guardar
        Cita nuevaCita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fechaHoraInicio(fechaInicioNueva)
                .fechaHoraFin(fechaFinNueva)
                .estado(EstadoCita.AGENDADA)
                .build();

        Cita citaGuardada = citaRepository.save(nuevaCita);

        return new CitaResponseDTO(
                citaGuardada.getId(),
                medico.getId(),
                medico.getUsuario().getNombre() + " " + medico.getUsuario().getApellido(),
                medico.getEspecialidad().getNombre(),
                citaGuardada.getFechaHoraInicio(),
                citaGuardada.getFechaHoraFin(),
                citaGuardada.getEstado().name()
        );
    }

    /**
     * Obtiene las próximas citas de un paciente autenticado.
     * @param emailPaciente El email del paciente extraído del token.
     * @return Una lista de DTOs de las próximas citas.
     */
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> obtenerProximasCitas(String emailPaciente) {
        Usuario paciente = usuarioRepository.findByEmail(emailPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        return citaRepository.findAllByPacienteIdAndFechaHoraInicioAfterOrderByFechaHoraInicioAsc(
                        paciente.getId(),
                        LocalDateTime.now()
                )
                .stream()
                .map(cita -> {
                    // Construimos el nombre completo del médico
                    String nombreCompletoMedico = cita.getMedico().getUsuario().getNombre() +
                            " " +
                            cita.getMedico().getUsuario().getApellido();

                    return new CitaResponseDTO(
                            cita.getId(),
                            cita.getMedico().getId(),
                            nombreCompletoMedico,
                            cita.getMedico().getEspecialidad().getNombre(),
                            cita.getFechaHoraInicio(),
                            cita.getFechaHoraFin(),
                            cita.getEstado().name()
                    );
                })
                .collect(Collectors.toList());
    }

    // Obtener todos los pacientes para el Admin
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> obtenerTodosLosPacientes() {
        return usuarioRepository.findByRolNombre("ROLE_PACIENTE").stream()
                .map(PacienteResponseDTO::new)
                .toList();
    }

    // Crear paciente desde panel administrativo
    @Transactional
    public PacienteResponseDTO crearPaciente(PacienteCreateDTO datos) {
        if (usuarioRepository.findByEmail(datos.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya existe");
        }

        var rolPaciente = rolRepository.findByNombre("ROLE_PACIENTE")
                .orElseThrow(() -> new IllegalStateException("Rol ROLE_PACIENTE no encontrado"));

        Usuario usuario = Usuario.builder()
                .nombre(datos.nombre())
                .apellido(datos.apellido())
                .email(datos.email())
                // Si no envían password, usamos el documento o una genérica
                .password(passwordEncoder.encode(datos.password() != null ? datos.password() : "12345678"))
                .documento(datos.documento())
                .telefono(datos.telefono())
                .direccion(datos.direccion())
                .fechaNacimiento(datos.fechaNacimiento())
                .genero(datos.genero())
                .rol(rolPaciente)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return new PacienteResponseDTO(guardado);
    }

    //Buscar un paciente por ID (para pre-llenar el formulario)
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPacientePorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getRol().getNombre().equals("ROLE_PACIENTE")) // Asegurar que sea paciente
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        return new PacienteResponseDTO(usuario);
    }

    //Actualizar paciente existente
    @Transactional
    public PacienteResponseDTO actualizarPaciente(Long id, PacienteActualizarDTO datos) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getRol().getNombre().equals("ROLE_PACIENTE"))
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // Validar que si cambia el email, no choque con otro existente
        if (!usuario.getEmail().equals(datos.email()) && usuarioRepository.findByEmail(datos.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso por otro usuario");
        }

        // Actualizar campos (Mapeo manual)
        usuario.setNombre(datos.nombre());
        usuario.setApellido(datos.apellido());
        usuario.setEmail(datos.email());
        usuario.setDocumento(datos.documento());
        usuario.setTelefono(datos.telefono());
        usuario.setDireccion(datos.direccion());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setGenero(datos.genero());

        // No actualizamos password aquí (eso iría en otro endpoint de seguridad)

        Usuario actualizado = usuarioRepository.save(usuario);
        return new PacienteResponseDTO(actualizado);
    }


}