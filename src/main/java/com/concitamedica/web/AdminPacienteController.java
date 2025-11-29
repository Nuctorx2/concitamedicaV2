package com.concitamedica.web;

import com.concitamedica.domain.paciente.PacienteService;
import com.concitamedica.domain.paciente.dto.PacienteActualizarDTO;
import com.concitamedica.domain.paciente.dto.PacienteCreateDTO;
import com.concitamedica.domain.paciente.dto.PacienteResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pacientes")
@RequiredArgsConstructor
public class AdminPacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO')")
    public ResponseEntity<List<PacienteResponseDTO>> listarPacientes() {
        return ResponseEntity.ok(pacienteService.obtenerTodosLosPacientes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PacienteResponseDTO> crearPaciente(@Valid @RequestBody PacienteCreateDTO datos) {
        return ResponseEntity.ok(pacienteService.crearPaciente(datos));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDICO')")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPacientePorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PacienteResponseDTO> actualizarPaciente(
            @PathVariable Long id,
            @Valid @RequestBody PacienteActualizarDTO datos) {
        return ResponseEntity.ok(pacienteService.actualizarPaciente(id, datos));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarPaciente(@PathVariable Long id) {
        pacienteService.eliminarPaciente(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivarPaciente(@PathVariable Long id) {
        pacienteService.reactivarPaciente(id);
        return ResponseEntity.noContent().build();
    }
}