package com.concitamedica.domain.horario;

import com.concitamedica.domain.horario.dto.CreacionHorarioDTO;
import com.concitamedica.domain.medico.Medico;
import com.concitamedica.domain.medico.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.concitamedica.domain.horario.dto.HorarioResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final MedicoRepository medicoRepository;
    private final com.concitamedica.domain.cita.CitaRepository citaRepository;

    @Transactional
    public Horario crearHorario(Long medicoId, CreacionHorarioDTO datos) {
        if (datos.horaFin().isBefore(datos.horaInicio()) || datos.horaFin().equals(datos.horaInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + medicoId));

        Horario nuevoHorario = Horario.builder()
                .medico(medico)
                .diaSemana(datos.diaSemana())
                .horaInicio(datos.horaInicio())
                .horaFin(datos.horaFin())
                .build();

        return horarioRepository.save(nuevoHorario);
    }

    @Transactional(readOnly = true)
    public List<HorarioResponseDTO> obtenerHorariosPorMedico(Long medicoId) {
        if (!medicoRepository.existsById(medicoId)) {
            throw new RuntimeException("Médico no encontrado con ID: " + medicoId);
        }

        return horarioRepository.findAllByMedicoId(medicoId)
                .stream()
                .map(HorarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarHorario(Long medicoId, Long horarioId) {
        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + horarioId));

        if (!horario.getMedico().getId().equals(medicoId)) {
            throw new SecurityException("Acceso denegado: El horario no pertenece al médico especificado.");
        }

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

        return horarioRepository.saveAll(nuevosHorarios);
    }

    @Transactional
    public List<Horario> reemplazarHorarios(Long medicoId, List<CreacionHorarioDTO> horariosDTO) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        horarioRepository.deleteAllByMedicoId(medicoId);
        horarioRepository.flush();

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

        validarYCancelarCitasConflictivas(medicoId, nuevosHorarios);

        return nuevosHorarios;
    }

    private void validarYCancelarCitasConflictivas(Long medicoId, List<Horario> nuevosHorarios) {

        List<com.concitamedica.domain.cita.Cita> citasFuturas =
                citaRepository.findByMedicoIdAndFechaHoraInicioAfterAndEstado(
                        medicoId, java.time.LocalDateTime.now(), com.concitamedica.domain.cita.EstadoCita.AGENDADA
                );

        for (com.concitamedica.domain.cita.Cita cita : citasFuturas) {
            boolean horarioValido = false;

            DiaSemana diaCita = DiaSemana.from(cita.getFechaHoraInicio().toLocalDate());
            LocalTime horaCita = cita.getFechaHoraInicio().toLocalTime();

            for (Horario horario : nuevosHorarios) {
                if (horario.getDiaSemana() == diaCita) {

                    if (!horaCita.isBefore(horario.getHoraInicio()) && horaCita.isBefore(horario.getHoraFin())) {
                        horarioValido = true;
                        break;
                    }
                }
            }

            if (!horarioValido) {
                cita.setEstado(com.concitamedica.domain.cita.EstadoCita.CANCELADA_ADMIN);
                citaRepository.save(cita);
            }
        }
    }
}