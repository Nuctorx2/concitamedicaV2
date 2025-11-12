package com.concitamedica.domain.medico;

import com.concitamedica.domain.cita.CitaRepository;
import com.concitamedica.domain.cita.dto.CitaMedicoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoAgendaService {

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;

    @Transactional(readOnly = true)
    public List<CitaMedicoResponseDTO> obtenerAgendaDelDia(String emailMedico, LocalDate fecha) {
        // 1. Buscar el perfil del médico a partir de su email de usuario
        Medico medico = medicoRepository.findByUsuarioEmail(emailMedico) // ¡Nuevo método de repositorio!
                .orElseThrow(() -> new RuntimeException("Perfil de médico no encontrado"));

        // 2. Definir el rango de búsqueda (el día completo)
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

        // 3. Buscar las citas y mapearlas al DTO
        return citaRepository.findAllByMedicoIdAndFechaHoraInicioBetween(medico.getId(), inicioDelDia, finDelDia)
                .stream()
                .map(cita -> new CitaMedicoResponseDTO(
                        cita.getId(),
                        cita.getPaciente().getNombre(),
                        cita.getFechaHoraInicio(),
                        cita.getFechaHoraFin(),
                        cita.getEstado().name()
                ))
                .collect(Collectors.toList());
    }
}