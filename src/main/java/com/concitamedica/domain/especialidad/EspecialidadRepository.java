package com.concitamedica.domain.especialidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Especialidad.
 */
@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    /**
     * Busca una especialidad por su nombre. Es útil para evitar duplicados.
     * @param nombre El nombre de la especialidad a buscar.
     * @return un Optional que contiene la especialidad si se encuentra.
     */
    Optional<Especialidad> findByNombre(String nombre);
}