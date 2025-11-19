package com.concitamedica.web;

import com.concitamedica.domain.cita.CitaRepository;
import com.concitamedica.domain.cita.dto.CitaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/citas")
@RequiredArgsConstructor
public class AdminCitaController {

    private final CitaRepository citaRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CitaResponseDTO>> listarTodasLasCitas() {
        // Nota: Idealmente esto iría en un servicio y con paginación,
        // pero para el MVP lo hacemos directo y mapeamos aquí.
        var citas = citaRepository.findAll().stream()
                .map(cita -> new CitaResponseDTO(
                        cita.getId(),
                        cita.getMedico().getId(),
                        cita.getMedico().getUsuario().getNombre() + " " + cita.getMedico().getUsuario().getApellido(),
                        cita.getMedico().getEspecialidad().getNombre(),
                        cita.getFechaHoraInicio(),
                        cita.getFechaHoraFin(),
                        cita.getEstado().name()
                )).toList();

        return ResponseEntity.ok(citas);
    }
}