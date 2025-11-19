package com.concitamedica.domain.cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findAllByMedicoIdAndFechaHoraInicioBetween(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<Cita> findAllByPacienteIdAndFechaHoraInicioAfterOrderByFechaHoraInicioAsc(Long pacienteId, LocalDateTime ahora);

    // 1. ¿Tiene el paciente una cita A ESTA HORA exacta?
    boolean existsByPacienteIdAndFechaHoraInicio(Long pacienteId, LocalDateTime fechaHoraInicio);

    // 2. ¿Tiene el paciente una cita CON ESTE MÉDICO en ESTE DÍA?
    // Usamos un rango de fechas para cubrir todo el día
    boolean existsByPacienteIdAndMedicoIdAndFechaHoraInicioBetween(
            Long pacienteId, Long medicoId, LocalDateTime inicioDia, LocalDateTime finDia
    );

    List<Cita> findAllByPacienteIdAndFechaHoraInicioBetween(
            Long pacienteId, LocalDateTime inicio, LocalDateTime fin
    );
}

