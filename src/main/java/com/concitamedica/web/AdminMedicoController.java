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

@RestController
@RequestMapping("/api/admin/medicos")
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
                .map(medico -> ResponseEntity.ok(medico))
                .orElse(ResponseEntity.notFound().build());
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
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminarMedico(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivarMedico(@PathVariable Long id) {
        medicoService.reactivarMedico(id);
        return ResponseEntity.noContent().build();
    }
}