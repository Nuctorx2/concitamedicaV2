package com.concitamedica.domain.medico;

import com.concitamedica.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Medico.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    List<Medico> findAllByEspecialidadId(Long especialidadId);
    Optional<Medico> findByUsuario(Usuario usuario);
    Optional<Medico> findByUsuarioEmail(String email);
}