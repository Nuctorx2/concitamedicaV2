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

    private final MedicoRepository medicoRepository;
    private final HorarioRepository horarioRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<DisponibilidadDTO> calcularDisponibilidad(Long medicoId, LocalDate fecha) {
        if (!medicoRepository.existsById(medicoId)) {
            throw new RuntimeException("Médico no encontrado");
        }

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        if (!medico.getUsuario().isEnabled()) {
            return new ArrayList<>();
        }

        DiaSemana dia = DiaSemana.from(fecha);
        Optional<Horario> horarioLaboralOpt = horarioRepository.findByMedicoIdAndDiaSemana(medicoId, dia);

        if (horarioLaboralOpt.isEmpty()) {
            return new ArrayList<>();
        }
        Horario horarioLaboral = horarioLaboralOpt.get();

        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);
        List<Cita> citasAgendadas = citaRepository.findAllByMedicoIdAndFechaHoraInicioBetween(medicoId, inicioDelDia, finDelDia);
        List<LocalTime> horasOcupadas = citasAgendadas.stream()
                .filter(cita -> cita.getEstado() == EstadoCita.AGENDADA)
                .map(cita -> cita.getFechaHoraInicio().toLocalTime())
                .toList();

        List<DisponibilidadDTO> slotsDisponibles = new ArrayList<>();
        LocalTime horaActual = horarioLaboral.getHoraInicio();

        while (horaActual.isBefore(horarioLaboral.getHoraFin())) {
            if (!horasOcupadas.contains(horaActual)) {
                slotsDisponibles.add(new DisponibilidadDTO(horaActual));
            }
            horaActual = horaActual.plusMinutes(30);
        }

        return slotsDisponibles;
    }

    @Transactional
    public CitaResponseDTO agendarCita(AgendarCitaDTO datosAgendamiento, String emailPaciente) {
        Usuario paciente = usuarioRepository.findByEmail(emailPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(datosAgendamiento.medicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        LocalDateTime fechaInicioNueva = datosAgendamiento.fechaHoraInicio();
        LocalDateTime fechaFinNueva = fechaInicioNueva.plusMinutes(30);

        if (fechaInicioNueva.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("No se pueden agendar citas en el pasado.");
        }

        boolean tieneCitaPendiente = citaRepository.tieneCitaActivaConEspecialidad(
                paciente.getId(),
                medico.getEspecialidad().getId(),
                LocalDateTime.now()
        );

        if (tieneCitaPendiente) {
            throw new IllegalStateException("Ya tienes una cita programada de "
                    + medico.getEspecialidad().getNombre() + " pendiente. "
                    + "Debes asistir a esa cita o cancelarla antes de agendar una nueva.");
        }

        LocalDateTime inicioDia = fechaInicioNueva.toLocalDate().atStartOfDay();
        LocalDateTime finDia = fechaInicioNueva.toLocalDate().atTime(LocalTime.MAX);

        List<Cita> citasDelDia = citaRepository.findAllByPacienteIdAndFechaHoraInicioBetween(
                paciente.getId(), inicioDia, finDia
        );

        for (Cita citaExistente : citasDelDia) {
            if (citaExistente.getEstado() != EstadoCita.AGENDADA) continue;

            boolean seSuperpone = fechaInicioNueva.isBefore(citaExistente.getFechaHoraFin()) &&
                    fechaFinNueva.isAfter(citaExistente.getFechaHoraInicio());

            if (seSuperpone) {
                throw new IllegalStateException("Conflicto de horario: Ya tienes una cita agendada ("
                        + citaExistente.getMedico().getUsuario().getNombre() + " - "
                        + citaExistente.getFechaHoraInicio().toLocalTime() + ") que se cruza con este horario.");
            }
        }

        List<DisponibilidadDTO> disponibilidad = calcularDisponibilidad(medico.getId(), fechaInicioNueva.toLocalDate());

        boolean slotDisponible = disponibilidad.stream()
                .anyMatch(slot -> slot.horaInicio().equals(fechaInicioNueva.toLocalTime()));

        if (!slotDisponible) {
            throw new IllegalStateException("El horario seleccionado ya no está disponible en la agenda del médico.");
        }

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
                paciente.getId(),
                medico.getUsuario().getNombre() + " " + medico.getUsuario().getApellido(),
                paciente.getNombre() + " " + paciente.getApellido(),
                medico.getEspecialidad().getNombre(),
                citaGuardada.getFechaHoraInicio(),
                citaGuardada.getFechaHoraFin(),
                citaGuardada.getEstado().name()
        );
    }

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
                    String nombreCompletoMedico = cita.getMedico().getUsuario().getNombre() +
                            " " +
                            cita.getMedico().getUsuario().getApellido();

                    return new CitaResponseDTO(
                            cita.getId(),
                            cita.getMedico().getId(),
                            cita.getPaciente().getId(),
                            nombreCompletoMedico,
                            cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellido(),
                            cita.getMedico().getEspecialidad().getNombre(),
                            cita.getFechaHoraInicio(),
                            cita.getFechaHoraFin(),
                            cita.getEstado().name()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> obtenerTodosLosPacientes() {
        return usuarioRepository.findByRolNombre("ROLE_PACIENTE").stream()
                .map(PacienteResponseDTO::new)
                .toList();
    }

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
                .password(passwordEncoder.encode(datos.password() != null ? datos.documento() : "12345678"))
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

    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPacientePorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getRol().getNombre().equals("ROLE_PACIENTE"))
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        return new PacienteResponseDTO(usuario);
    }

    @Transactional
    public PacienteResponseDTO actualizarPaciente(Long id, PacienteActualizarDTO datos) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getRol().getNombre().equals("ROLE_PACIENTE"))
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        if (!usuario.getEmail().equals(datos.email()) && usuarioRepository.findByEmail(datos.email()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso por otro usuario");
        }

        usuario.setNombre(datos.nombre());
        usuario.setApellido(datos.apellido());
        usuario.setEmail(datos.email());
        usuario.setDocumento(datos.documento());
        usuario.setTelefono(datos.telefono());
        usuario.setDireccion(datos.direccion());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setGenero(datos.genero());

        Usuario actualizado = usuarioRepository.save(usuario);
        return new PacienteResponseDTO(actualizado);
    }

    @Transactional
    public void eliminarPaciente(Long id) {
        Usuario paciente = usuarioRepository.findById(id)
                .filter(u -> u.getRol().getNombre().equals("ROLE_PACIENTE"))
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        paciente.setEnabled(false);
        usuarioRepository.save(paciente);

        List<Cita> citasFuturas = citaRepository.findAllByPacienteIdAndFechaHoraInicioBetween(
                id, LocalDateTime.now(), LocalDateTime.now().plusYears(2)
        );

        for (Cita cita : citasFuturas) {
            if (cita.getEstado() == EstadoCita.AGENDADA) {
                cita.setEstado(EstadoCita.CANCELADA_ADMIN);
                citaRepository.save(cita);
            }
        }
    }

    @Transactional
    public void reactivarPaciente(Long id) {
        Usuario paciente = usuarioRepository.findById(id)
                .filter(u -> u.getRol().getNombre().equals("ROLE_PACIENTE"))
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        paciente.setEnabled(true);
        usuarioRepository.save(paciente);
    }
}