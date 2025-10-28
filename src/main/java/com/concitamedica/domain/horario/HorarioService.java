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

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final MedicoRepository medicoRepository;

    /**
     * Crea un nuevo bloque de horario y lo asocia a un médico existente.
     * @param medicoId El ID del médico al que se le añadirá el horario.
     * @param datos DTO con la información del nuevo horario.
     * @return La entidad Horario recién creada.
     */
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

    /**
     * Obtiene todos los bloques de horario de un médico específico.
     * @param medicoId El ID del médico cuyos horarios se quieren obtener.
     * @return Una lista de DTOs con la información de cada bloque de horario.
     */
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

    /**
     * Elimina un bloque de horario específico.
     * @param medicoId El ID del médico al que pertenece el horario (para validación).
     * @param horarioId El ID del horario a eliminar.
     */
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

    /**
     * Crea múltiples bloques de horario para un médico en una sola transacción.
     * @param medicoId El ID del médico.
     * @param horariosDTO La lista de DTOs de horarios a crear.
     * @return La lista de entidades Horario recién creadas.
     */
    @Transactional
    public List<Horario> crearHorariosEnLote(Long medicoId, List<CreacionHorarioDTO> horariosDTO) {
        // 1. Buscar el médico.
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + medicoId));

        List<Horario> nuevosHorarios = new ArrayList<>();

        // 2. Iterar sobre la lista de DTOs, validar y convertir cada uno.
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
}