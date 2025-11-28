package com.concitamedica.domain.reporte;

import com.concitamedica.domain.medico.MedicoService;
import com.concitamedica.domain.medico.dto.MedicoResponseDTO;
import com.concitamedica.domain.paciente.PacienteService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final MedicoService medicoService;
    private final PacienteService pacienteService;
    private final com.concitamedica.domain.cita.CitaRepository citaRepository;

    public byte[] generarReporteMedicos() throws JRException {

        List<MedicoResponseDTO> medicos = medicoService.obtenerTodosLosMedicos();

        java.io.InputStream inputStream = this.getClass().getResourceAsStream("/reports/medicos_report.jrxml");

        if (inputStream == null) {
            throw new JRException("No se encontró el archivo del reporte: /reports/medicos_report.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(medicos);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "ConCitaMedica Admin");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generarReportePacientes() throws JRException {
        List<com.concitamedica.domain.paciente.dto.PacienteResponseDTO> pacientes = pacienteService.obtenerTodosLosPacientes();

        java.io.InputStream inputStream = this.getClass().getResourceAsStream("/reports/pacientes_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pacientes);
        Map<String, Object> parameters = new HashMap<>();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generarReporteCitas() throws JRException {

        List<com.concitamedica.domain.cita.dto.CitaResponseDTO> citasDTO = citaRepository.findAllByOrderByFechaHoraInicioDesc().stream()
                .map(cita -> new com.concitamedica.domain.cita.dto.CitaResponseDTO(
                        cita.getId(),
                        cita.getMedico().getId(),
                        cita.getPaciente().getId(),
                        cita.getMedico().getUsuario().getNombre() + " " + cita.getMedico().getUsuario().getApellido(),
                        cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellido(),
                        cita.getMedico().getEspecialidad().getNombre(),
                        cita.getFechaHoraInicio(),
                        cita.getFechaHoraFin(),
                        cita.getEstado().name()
                )).toList();

        java.io.InputStream inputStream = this.getClass().getResourceAsStream("/reports/citas_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(citasDTO);
        Map<String, Object> parameters = new HashMap<>();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}