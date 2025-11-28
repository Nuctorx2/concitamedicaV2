package com.concitamedica.web;

import com.concitamedica.domain.reporte.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/medicos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarReporteMedicos() {
        try {
            byte[] reportePdf = reporteService.generarReporteMedicos();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicos_reporte.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(reportePdf);

        } catch (JRException e) {
            log.error("Error generando el reporte de médicos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pacientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarReportePacientes() {
        try {
            byte[] reportePdf = reporteService.generarReportePacientes();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pacientes_reporte.pdf");
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(reportePdf);
        } catch (JRException e) {
            log.error("Error reporte pacientes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/citas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> descargarReporteCitas() {
        try {
            byte[] reportePdf = reporteService.generarReporteCitas();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=citas_reporte.pdf");
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(reportePdf);
        } catch (JRException e) {
            log.error("Error reporte citas", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}