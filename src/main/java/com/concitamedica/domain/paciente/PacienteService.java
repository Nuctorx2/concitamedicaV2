package com.concitamedica.domain.paciente;

import com.concitamedica.domain.cita.Cita;
import com.concitamedica.domain.cita.CitaRepository;
import com.concitamedica.domain.cita.dto.CitaResponseDTO;
import com.concitamedica.domain.horario.DiaSemana;
import com.concitamedica.domain.horario.Horario;
import com.concitamedica.domain.horario.HorarioRepository;
import com.concitamedica.domain.paciente.dto.DisponibilidadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.concitamedica.domain.cita.EstadoCita;
import com.concitamedica.domain.paciente.dto.AgendarCitaDTO;
import com.concitamedica.domain.usuario.Usuario;
import com.concitamedica.domain.usuario.UsuarioRepository;
import com.concitamedica.domain.medico.Medico;

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
        // 1. Obtener las entidades necesarias de la BD
        Usuario paciente = usuarioRepository.findByEmail(emailPaciente)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(datosAgendamiento.medicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        LocalDateTime fechaHoraInicio = datosAgendamiento.fechaHoraInicio();

        // 2. Validar que el slot esté disponible (¡Lógica CRÍTICA!)
        List<DisponibilidadDTO> disponibilidad = calcularDisponibilidad(medico.getId(), fechaHoraInicio.toLocalDate());

        boolean slotDisponible = disponibilidad.stream()
                .anyMatch(slot -> slot.horaInicio().equals(fechaHoraInicio.toLocalTime()));

        if (!slotDisponible) {
            throw new IllegalStateException("El horario seleccionado ya no está disponible.");
        }

        // 3. Crear y guardar la nueva cita
        // Crear y guardar
        Cita nuevaCita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fechaHoraInicio(fechaHoraInicio)
                .fechaHoraFin(fechaHoraInicio.plusMinutes(30))
                .estado(EstadoCita.AGENDADA)
                .build();

        Cita citaGuardada = citaRepository.save(nuevaCita);
        
        return new CitaResponseDTO(
                citaGuardada.getId(),
                medico.getId(),
                medico.getUsuario().getNombre(), // O el campo correspondiente
                citaGuardada.getFechaHoraInicio(),
                citaGuardada.getFechaHoraFin(),
                citaGuardada.getEstado().name()
        );
    }
}