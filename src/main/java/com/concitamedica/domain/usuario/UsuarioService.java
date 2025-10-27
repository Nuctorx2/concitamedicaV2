package com.concitamedica.domain.usuario;

import com.concitamedica.domain.rol.Rol;
import com.concitamedica.domain.rol.RolRepository;
import com.concitamedica.domain.usuario.dto.RegistroUsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService{

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario con el rol de PACIENTE.
     * @param datosRegistro DTO con la información del nuevo usuario.
     * @return La entidad Usuario que fue guardada en la base de datos.
     */
    @Transactional // Indica que este método es una transacción. Si algo falla, se revierte todo.
    public Usuario registrarPaciente(RegistroUsuarioDTO datosRegistro) {
        // 1. Validar que el email no esté en uso.
        if (usuarioRepository.findByEmail(datosRegistro.email()).isPresent()) {
            // Es mejor lanzar una excepción específica, pero por ahora esto funciona.
            throw new IllegalStateException("El correo electrónico ya está en uso.");
        }

        // 2. Buscar el rol "ROLE_PACIENTE".
        Rol rolPaciente = rolRepository.findByNombre("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("El rol de Paciente no existe en la base de datos."));

        // 3. Crear la nueva entidad Usuario.
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(datosRegistro.nombre())
                .email(datosRegistro.email())
                .password(passwordEncoder.encode(datosRegistro.password()))
                .fechaNacimiento(datosRegistro.fechaNacimiento())
                .genero(datosRegistro.genero())
                .rol(rolPaciente)
                .build();

        // 4. Guardar en la base de datos y retornar el usuario guardado.
        return usuarioRepository.save(nuevoUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                true, true, true, true, // Opciones de cuenta (habilitada, etc.)
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol().getNombre()))
        );
    }
}