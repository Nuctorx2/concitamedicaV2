package com.concitamedica.web;

import com.concitamedica.domain.cita.dto.CitaMedicoResponseDTO;
import com.concitamedica.domain.medico.MedicoAgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medicos/agenda")
@RequiredArgsConstructor
public class MedicoAgendaController {

    private final MedicoAgendaService medicoAgendaService;

    @GetMapping("/dia")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<List<CitaMedicoResponseDTO>> obtenerAgendaDelDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication authentication) {

        String emailMedico = authentication.getName();
        List<CitaMedicoResponseDTO> agenda = medicoAgendaService.obtenerAgendaDelDia(emailMedico, fecha);
        return ResponseEntity.ok(agenda);
    }
}
