package com.concitamedica.web;

import com.concitamedica.domain.horario.Horario;
import com.concitamedica.domain.horario.HorarioService;
import com.concitamedica.domain.horario.dto.CreacionHorarioDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.concitamedica.domain.horario.dto.HorarioResponseDTO;
import java.util.List;

@RestController
@RequestMapping("/api/admin/medicos/{medicoId}/horarios") // ✅ URL anidada
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    /**
     * Endpoint para añadir un nuevo bloque de horario a un médico.
     * Solo accesible para usuarios con el rol 'ADMIN'.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Horario> anadirHorario(
            @PathVariable Long medicoId,
            @Valid @RequestBody CreacionHorarioDTO datos) {
        try {
            Horario nuevoHorario = horarioService.crearHorario(medicoId, datos);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoHorario);
        } catch (IllegalArgumentException e) {
            // Si el servicio lanza un error de validación (ej. horaFin antes de horaInicio)
            return ResponseEntity.badRequest().build(); // Devuelve 400 Bad Request
        } catch (RuntimeException e) {
            // Si el médico no se encuentra
            return ResponseEntity.notFound().build(); // Devuelve 404 Not Found
        }
    }

    /**
     * Endpoint para añadir múltiples bloques de horario a un médico en una sola petición.
     */
    @PostMapping("/lote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Horario>> anadirHorariosEnLote(
            @PathVariable Long medicoId,
            @Valid @RequestBody List<CreacionHorarioDTO> horariosDTO) {
        try {
            List<Horario> nuevosHorarios = horarioService.crearHorariosEnLote(medicoId, horariosDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevosHorarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para obtener todos los horarios de un médico específico.
     * Solo accesible para usuarios con el rol 'ADMIN'.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HorarioResponseDTO>> obtenerHorariosPorMedico(@PathVariable Long medicoId) {
        try {
            List<HorarioResponseDTO> horarios = horarioService.obtenerHorariosPorMedico(medicoId);
            return ResponseEntity.ok(horarios);
        } catch (RuntimeException e) {
            // Si el médico no se encuentra
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para eliminar un bloque de horario de un médico.
     * Solo accesible para usuarios con el rol 'ADMIN'.
     */
    @DeleteMapping("/{horarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarHorario(
            @PathVariable Long medicoId,
            @PathVariable Long horarioId) {
        try {
            horarioService.eliminarHorario(medicoId, horarioId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (SecurityException e) {
            // Si el horario no pertenece al médico
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        } catch (RuntimeException e) {
            // Si el horario o el médico no se encuentran
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // Endpoint para reemplazar toda la agenda
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HorarioResponseDTO>> actualizarHorarios(
            @PathVariable Long medicoId,
            @Valid @RequestBody List<CreacionHorarioDTO> horariosDTO) {

        List<Horario> horariosGuardados = horarioService.reemplazarHorarios(medicoId, horariosDTO);

        List<HorarioResponseDTO> respuesta = horariosGuardados.stream()
                .map(HorarioResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(respuesta);
    }
}