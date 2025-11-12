package com.concitamedica.domain.cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findAllByMedicoIdAndFechaHoraInicioBetween(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // ✅ NUEVO MÉTODO
    /**
     * Busca todas las citas de un paciente específico cuya fecha de inicio sea
     * posterior a la fecha y hora proporcionadas, ordenadas por fecha de inicio.
     * @param pacienteId El ID del usuario paciente.
     * @param ahora La fecha y hora actual.
     * @return Una lista de las próximas citas del paciente.
     */
    List<Cita> findAllByPacienteIdAndFechaHoraInicioAfterOrderByFechaHoraInicioAsc(Long pacienteId, LocalDateTime ahora);
}

