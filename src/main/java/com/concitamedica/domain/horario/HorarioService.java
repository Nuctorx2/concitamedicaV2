package com.concitamedica.domain.horario;

import com.concitamedica.domain.horario.dto.CreacionHorarioDTO;
import com.concitamedica.domain.medico.Medico;
import com.concitamedica.domain.medico.MedicoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.concitamedica.domain.horario.dto.HorarioResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final MedicoRepository medicoRepository;
    private final com.concitamedica.domain.cita.CitaRepository citaRepository;

    @Transactional
    public Horario crearHorario(Long medicoId, CreacionHorarioDTO datos) {
        // 1. Validar que la hora de fin sea posterior a la de inicio.
        if (datos.horaFin().isBefore(datos.horaInicio()) || datos.horaFin().equals(datos.horaInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        // 2. Buscar el médico. Si no existe, lanza una excepción.
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + medicoId));

        // 3. Crear la nueva entidad Horario.
        Horario nuevoHorario = Horario.builder()
                .medico(medico)
                .diaSemana(datos.diaSemana())
                .horaInicio(datos.horaInicio())
                .horaFin(datos.horaFin())
                .build();

        // 4. Guardar en la base de datos.
        return horarioRepository.save(nuevoHorario);
    }

    @Transactional(readOnly = true)
    public List<HorarioResponseDTO> obtenerHorariosPorMedico(Long medicoId) {
        // 1. Verificar si el médico existe.
        if (!medicoRepository.existsById(medicoId)) {
            throw new RuntimeException("Médico no encontrado con ID: " + medicoId);
        }

        // 2. ¡Ahora esto funciona sin advertencias!
        return horarioRepository.findAllByMedicoId(medicoId)
                .stream()
                .map(HorarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarHorario(Long medicoId, Long horarioId) {
        // 1. Buscar el horario por su ID.
        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + horarioId));

        // 2. ¡Validación de seguridad importante!
        // Nos aseguramos de que el horario que se quiere borrar realmente pertenezca
        // al médico especificado en la URL.
        if (!horario.getMedico().getId().equals(medicoId)) {
            throw new SecurityException("Acceso denegado: El horario no pertenece al médico especificado.");
        }

        // 3. Si todo está en orden, eliminar el horario.
        horarioRepository.delete(horario);
    }

    @Transactional
    public List<Horario> crearHorariosEnLote(Long medicoId, List<CreacionHorarioDTO> horariosDTO) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + medicoId));

        List<Horario> nuevosHorarios = new ArrayList<>();

        for (CreacionHorarioDTO dto : horariosDTO) {
            if (dto.horaFin().isBefore(dto.horaInicio()) || dto.horaFin().equals(dto.horaInicio())) {
                throw new IllegalArgumentException(
                        "Error en el horario para " + dto.diaSemana() + ": la hora de fin debe ser posterior a la de inicio."
                );
            }

            Horario nuevoHorario = Horario.builder()
                    .medico(medico)
                    .diaSemana(dto.diaSemana())
                    .horaInicio(dto.horaInicio())
                    .horaFin(dto.horaFin())
                    .build();
            nuevosHorarios.add(nuevoHorario);
        }

        // 3. Guardar todos los nuevos horarios a la vez.
        return horarioRepository.saveAll(nuevosHorarios);
    }

    @Transactional
    public List<Horario> reemplazarHorarios(Long medicoId, List<CreacionHorarioDTO> horariosDTO) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        // 1. Eliminar horarios viejos
        horarioRepository.deleteAllByMedicoId(medicoId);
        horarioRepository.flush();

        // 2. Guardar nuevos horarios
        List<Horario> nuevosHorarios = horariosDTO.stream()
                .map(dto -> {
                    if (dto.horaInicio().isAfter(dto.horaFin())) {
                        throw new IllegalArgumentException("Error en horario " + dto.diaSemana() + ": Inicio posterior a fin.");
                    }
                    return Horario.builder()
                            .medico(medico)
                            .diaSemana(dto.diaSemana())
                            .horaInicio(dto.horaInicio())
                            .horaFin(dto.horaFin())
                            .build();
                })
                .map(horarioRepository::save)
                .toList();

        // 3. 🧹 AUDITORÍA DE CITAS (Limpieza de conflictos)
        validarYCancelarCitasConflictivas(medicoId, nuevosHorarios);

        return nuevosHorarios;
    }

    private void validarYCancelarCitasConflictivas(Long medicoId, List<Horario> nuevosHorarios) {
        // Traer citas futuras activas
        List<com.concitamedica.domain.cita.Cita> citasFuturas =
                citaRepository.findByMedicoIdAndFechaHoraInicioAfterAndEstado(
                        medicoId, java.time.LocalDateTime.now(), com.concitamedica.domain.cita.EstadoCita.AGENDADA
                );

        for (com.concitamedica.domain.cita.Cita cita : citasFuturas) {
            boolean horarioValido = false;

            // Convertir fecha cita a DiaSemana (LUNES, MARTES...)
            DiaSemana diaCita = DiaSemana.from(cita.getFechaHoraInicio().toLocalDate());
            LocalTime horaCita = cita.getFechaHoraInicio().toLocalTime();

            // Buscar si existe un horario nuevo que cubra este día y hora
            for (Horario horario : nuevosHorarios) {
                if (horario.getDiaSemana() == diaCita) {
                    // Verificar si la hora de la cita está dentro del rango nuevo
                    // (horaCita >= inicio && horaCita < fin)
                    if (!horaCita.isBefore(horario.getHoraInicio()) && horaCita.isBefore(horario.getHoraFin())) {
                        horarioValido = true;
                        break;
                    }
                }
            }

            // Si después de revisar los nuevos horarios, la cita quedó "fuera de lugar"
            if (!horarioValido) {
                cita.setEstado(com.concitamedica.domain.cita.EstadoCita.CANCELADA_ADMIN);
                // Opcional: Podrías agregar un campo 'motivoCancelacion' en la entidad Cita
                // cita.setMotivo("Cambio de horario del médico");
                citaRepository.save(cita);
            }
        }
    }
}