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
@RequestMapping("/api/admin/medicos/{medicoId}/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Horario> anadirHorario(
            @PathVariable Long medicoId,
            @Valid @RequestBody CreacionHorarioDTO datos) {
        try {
            Horario nuevoHorario = horarioService.crearHorario(medicoId, datos);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoHorario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HorarioResponseDTO>> obtenerHorariosPorMedico(@PathVariable Long medicoId) {
        try {
            List<HorarioResponseDTO> horarios = horarioService.obtenerHorariosPorMedico(medicoId);
            return ResponseEntity.ok(horarios);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{horarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarHorario(
            @PathVariable Long medicoId,
            @PathVariable Long horarioId) {
        try {
            horarioService.eliminarHorario(medicoId, horarioId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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