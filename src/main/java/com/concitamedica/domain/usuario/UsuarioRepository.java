package com.concitamedica.domain.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Importante

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su dirección de email.
     * @param email El email a buscar.
     * @return un Optional que contiene al usuario si se encuentra, o un Optional vacío si no.
     */
    Optional<Usuario> findByEmail(String email);
}