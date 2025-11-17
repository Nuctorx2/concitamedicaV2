package com.concitamedica.web;

import com.concitamedica.domain.especialidad.EspecialidadService;
import com.concitamedica.domain.especialidad.dto.EspecialidadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    /**
     * Endpoint público (o para usuarios autenticados) para llenar selects.
     */
    @GetMapping
    public ResponseEntity<List<EspecialidadDTO>> listarEspecialidades() {
        return ResponseEntity.ok(especialidadService.obtenerTodas());
    }
}