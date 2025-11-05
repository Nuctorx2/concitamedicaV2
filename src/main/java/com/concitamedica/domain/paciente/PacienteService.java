package com.concitamedica.domain.paciente;

import com.concitamedica.domain.cita.Cita;
import com.concitamedica.domain.cita.CitaRepository;
import com.concitamedica.domain.horario.DiaSemana;
import com.concitamedica.domain.horario.Horario;
import com.concitamedica.domain.horario.HorarioRepository;
import com.concitamedica.domain.paciente.dto.DisponibilidadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}