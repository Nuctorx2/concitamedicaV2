package com.concitamedica.web;

import com.concitamedica.domain.cita.dto.CitaResponseDTO;
import com.concitamedica.domain.paciente.PacienteService;
import com.concitamedica.domain.paciente.dto.DisponibilidadDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.concitamedica.domain.rol.Roles;
import com.concitamedica.domain.cita.Cita;
import com.concitamedica.domain.paciente.dto.AgendarCitaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;


import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping("/medicos/{medicoId}/disponibilidad")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<DisponibilidadDTO>> obtenerDisponibilidad(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<DisponibilidadDTO> disponibilidad = pacienteService.calcularDisponibilidad(medicoId, fecha);
        return ResponseEntity.ok(disponibilidad);
    }

    /**
     * Endpoint para que un paciente agende una nueva cita.
     */
    @PostMapping("/citas")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<CitaResponseDTO> agendarCita(
            @RequestBody @Valid AgendarCitaDTO datosAgendamiento,
            Authentication authentication) {

        // El email del usuario autenticado se extrae del objeto Authentication
        String emailPaciente = authentication.getName();

        try {
            CitaResponseDTO nuevaCita = pacienteService.agendarCita(datosAgendamiento, emailPaciente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (IllegalStateException e) {
            // Capturamos el error si el horario ya no está disponible
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 Conflict
        }
    }

    /**
     * Endpoint para que un paciente vea su lista de próximas citas.
     */
    @GetMapping("/citas/proximas")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<CitaResponseDTO>> obtenerProximasCitas(Authentication authentication) {
        String emailPaciente = authentication.getName();
        log.info("Iniciando búsqueda de próximas citas para paciente: {}", emailPaciente);
        List<CitaResponseDTO> proximasCitas = pacienteService.obtenerProximasCitas(emailPaciente);
        log.debug("Se encontraron {} citas para el paciente: {}", proximasCitas.size(), emailPaciente);
        return ResponseEntity.ok(proximasCitas);
    }
}