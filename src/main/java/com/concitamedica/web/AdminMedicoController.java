package com.concitamedica.web;

import com.concitamedica.domain.medico.Medico;
import com.concitamedica.domain.medico.MedicoService;
import com.concitamedica.domain.medico.dto.ActualizacionMedicoDTO;
import com.concitamedica.domain.medico.dto.CreacionMedicoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import com.concitamedica.domain.medico.dto.MedicoResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.concitamedica.domain.rol.Roles;

@RestController
@RequestMapping("/api/admin/medicos") // ✅ URL base para la administración de médicos
@RequiredArgsConstructor
public class AdminMedicoController {

    private final MedicoService medicoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Medico> crearMedico(@Valid @RequestBody CreacionMedicoDTO datos) {
        Medico medicoCreado = medicoService.crearMedico(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoCreado);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MedicoResponseDTO>> obtenerTodos() {
        List<MedicoResponseDTO> medicos = medicoService.obtenerTodosLosMedicos();
        return ResponseEntity.ok(medicos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return medicoService.obtenerMedicoPorId(id)
                .map(medico -> ResponseEntity.ok(medico)) // Si se encuentra, devuelve 200 OK con el médico
                .orElse(ResponseEntity.notFound().build()); // Si no, devuelve un 404 Not Found
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizacionMedicoDTO datos) {
        try {
            MedicoResponseDTO medicoActualizado = medicoService.actualizarMedico(id, datos);
            return ResponseEntity.ok(medicoActualizado);
        } catch (RuntimeException e) {
            // Si el servicio lanza una excepción (ej. médico no encontrado), devolvemos 404.
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminarMedico(id);
        // Para una operación DELETE exitosa, se devuelve 204 No Content.
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivarMedico(@PathVariable Long id) {
        medicoService.reactivarMedico(id);
        return ResponseEntity.noContent().build();
    }
}