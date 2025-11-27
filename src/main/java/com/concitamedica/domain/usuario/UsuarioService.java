package com.concitamedica.domain.usuario;

import com.concitamedica.domain.rol.Rol;
import com.concitamedica.domain.rol.RolRepository;
import com.concitamedica.domain.usuario.dto.CambioPasswordDTO;
import com.concitamedica.domain.usuario.dto.PerfilUpdateDTO;
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
     * Registra un nuevo usuario forzando el rol de PACIENTE.
     * SEGURIDAD: Ignoramos el campo 'rol' del DTO para evitar elevación de privilegios.
     */
    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO datosRegistro) {
        if (usuarioRepository.findByEmail(datosRegistro.email()).isPresent()) {
            throw new IllegalStateException("El correo electrónico ya está en uso.");
        }

        String nombreRol = "ROLE_PACIENTE";

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("El rol " + nombreRol + " no existe en la BDD."));

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(datosRegistro.nombre())
                .apellido(datosRegistro.apellido())
                .documento(datosRegistro.documento())
                .telefono(datosRegistro.telefono())
                .direccion(datosRegistro.direccion())
                .email(datosRegistro.email())
                .password(passwordEncoder.encode(datosRegistro.password()))
                .fechaNacimiento(datosRegistro.fechaNacimiento())
                .genero(datosRegistro.genero())
                .rol(rol)
                .build();
        return usuarioRepository.save(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = buscarPorEmail(username); // Reutilizamos el método

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.isEnabled(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol().getNombre()))
        );
    }

    @Transactional
    public Usuario actualizarPerfil(String email, PerfilUpdateDTO datos) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(datos.nombre());
        usuario.setApellido(datos.apellido());
        usuario.setDocumento(datos.documento());
        usuario.setTelefono(datos.telefono());
        usuario.setDireccion(datos.direccion());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setGenero(datos.genero());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(String email, CambioPasswordDTO datos) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(datos.passwordActual(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        // 2. Encriptar y guardar la nueva
        usuario.setPassword(passwordEncoder.encode(datos.nuevaPassword()));
        usuarioRepository.save(usuario);
    }
}