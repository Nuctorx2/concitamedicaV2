package com.concitamedica.domain.cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findAllByMedicoIdAndFechaHoraInicioBetween(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}

