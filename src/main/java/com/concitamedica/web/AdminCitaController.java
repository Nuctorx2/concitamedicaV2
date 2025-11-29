package com.concitamedica.web;

import com.concitamedica.domain.cita.CitaRepository;
import com.concitamedica.domain.cita.dto.CitaResponseDTO;
import com.concitamedica.domain.cita.EstadoCita;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.concitamedica.domain.cita.Cita;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/citas")
@RequiredArgsConstructor
public class AdminCitaController {

    private final CitaRepository citaRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CitaResponseDTO>> listarTodasLasCitas() {
        List<CitaResponseDTO> citas = citaRepository.findAllByOrderByFechaHoraInicioDesc().stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(citas);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(EstadoCita.CANCELADA_ADMIN);
        citaRepository.save(cita);

        return ResponseEntity.noContent().build();
    }

    private CitaResponseDTO mapToDTO(Cita cita) {
        String nombreMedico = cita.getMedico().getUsuario().getNombre() + " " + cita.getMedico().getUsuario().getApellido();
        String nombrePaciente = cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellido();

        return new CitaResponseDTO(
                cita.getId(),
                cita.getMedico().getId(),
                cita.getPaciente().getId(),
                nombreMedico,
                nombrePaciente,
                cita.getMedico().getEspecialidad().getNombre(),
                cita.getFechaHoraInicio(),
                cita.getFechaHoraFin(),
                cita.getEstado().name()
        );
    }
}