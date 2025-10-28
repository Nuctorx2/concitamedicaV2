package com.concitamedica.web;

import com.concitamedica.domain.paciente.PacienteService;
import com.concitamedica.domain.paciente.dto.DisponibilidadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.concitamedica.domain.rol.Roles;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping("/medicos/{medicoId}/disponibilidad")
    @PreAuthorize("hasRole(" + Roles.PACIENTE + ")")
    public ResponseEntity<List<DisponibilidadDTO>> obtenerDisponibilidad(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<DisponibilidadDTO> disponibilidad = pacienteService.calcularDisponibilidad(medicoId, fecha);
        return ResponseEntity.ok(disponibilidad);
    }
}