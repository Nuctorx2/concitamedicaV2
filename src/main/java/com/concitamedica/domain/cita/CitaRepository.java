package com.concitamedica.domain.cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findAllByMedicoIdAndFechaHoraInicioBetween(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    List<Cita> findAllByPacienteIdAndFechaHoraInicioAfterOrderByFechaHoraInicioAsc(Long pacienteId, LocalDateTime ahora);

    boolean existsByPacienteIdAndFechaHoraInicio(Long pacienteId, LocalDateTime fechaHoraInicio);

    boolean existsByPacienteIdAndMedicoIdAndFechaHoraInicioBetween(
            Long pacienteId, Long medicoId, LocalDateTime inicioDia, LocalDateTime finDia
    );

    List<Cita> findAllByPacienteIdAndFechaHoraInicioBetween(
            Long pacienteId, LocalDateTime inicio, LocalDateTime fin
    );

    List<Cita> findAllByOrderByFechaHoraInicioDesc();

    void deleteAllByPacienteId(Long pacienteId);

    List<Cita> findByMedicoIdAndFechaHoraInicioAfterAndEstado(Long medicoId, LocalDateTime fecha, EstadoCita estado);

    void deleteAllByMedicoId(Long medicoId);

    List<Cita> findAllByMedicoIdAndFechaHoraInicioAfterOrderByFechaHoraInicioAsc(Long medicoId, LocalDateTime fecha);

    @Query("SELECT COUNT(c) > 0 FROM Cita c " +
            "WHERE c.paciente.id = :pacienteId " +
            "AND c.medico.especialidad.id = :especialidadId " +
            "AND c.estado = 'AGENDADA' " +
            "AND c.fechaHoraInicio > :fechaActual")
    boolean tieneCitaActivaConEspecialidad(
            @Param("pacienteId") Long pacienteId,
            @Param("especialidadId") Long especialidadId,
            @Param("fechaActual") LocalDateTime fechaActual
    );


}

