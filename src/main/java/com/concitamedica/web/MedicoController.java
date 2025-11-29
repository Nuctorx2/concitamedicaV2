package com.concitamedica.web;

import com.concitamedica.domain.medico.MedicoService;
import com.concitamedica.domain.medico.dto.MedicoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN', 'MEDICO')")
    public ResponseEntity<List<MedicoResponseDTO>> buscarPorEspecialidad(
            @RequestParam Long especialidadId) {

        List<MedicoResponseDTO> medicos = medicoService.buscarPorEspecialidad(especialidadId);
        return ResponseEntity.ok(medicos);
    }
}