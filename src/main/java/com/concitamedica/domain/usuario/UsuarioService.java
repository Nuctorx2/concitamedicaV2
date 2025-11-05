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
    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO datosRegistro) {
        if (usuarioRepository.findByEmail(datosRegistro.email()).isPresent()) {
            throw new IllegalStateException("El correo electrónico ya está en uso.");
        }

        String nombreRol = "ROLE_" + datosRegistro.rol().toUpperCase();

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("El rol" + nombreRol + " de Paciente no existe en la base de datos."));

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(datosRegistro.nombre())
                .email(datosRegistro.email())
                .password(passwordEncoder.encode(datosRegistro.password()))
                .fechaNacimiento(datosRegistro.fechaNacimiento())
                .genero(datosRegistro.genero())
                .rol(rol)
                .build();
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