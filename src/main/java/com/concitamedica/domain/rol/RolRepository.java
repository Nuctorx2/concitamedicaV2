package com.concitamedica.domain.rol;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Rol.
 * Permite realizar operaciones CRUD y consultas personalizadas sobre la tabla 'roles'.
 */
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);
}
