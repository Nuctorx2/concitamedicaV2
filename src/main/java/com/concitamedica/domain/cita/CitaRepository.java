package com.concitamedica.domain.cita;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    // Más adelante añadiremos aquí métodos para buscar citas por médico y fecha.
}

