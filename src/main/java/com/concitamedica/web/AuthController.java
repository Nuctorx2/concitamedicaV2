package com.concitamedica.web;

import com.concitamedica.domain.usuario.Usuario;
import com.concitamedica.domain.usuario.UsuarioService;
import com.concitamedica.domain.usuario.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.concitamedica.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.concitamedica.domain.usuario.dto.UsuarioResponseDTO;

/**
 * Controlador REST para manejar las operaciones de autenticación como registro y login.
 */
@RestController // Combina @Controller y @ResponseBody. Indica que los métodos devuelven datos (JSON).
@RequestMapping("/api/auth") // Define la URL base para todos los endpoints de este controlador.
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Endpoint para registrar un nuevo paciente.
     * Escucha en POST /api/auth/register.
     * @param datosRegistro El cuerpo de la petición HTTP, convertido a un DTO.
     * @return Una respuesta HTTP con el usuario creado y un estado 201 (Created).
     */
    @PostMapping("/register")
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody RegistroUsuarioDTO datosRegistro) {
        Usuario nuevoUsuario = usuarioService.registrarUsuario(datosRegistro);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        // 1. Spring Security se encarga de la autenticación.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        // 2. Si la autenticación es exitosa, generamos el token.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioActual(Authentication authentication) {
        // authentication.getName() obtiene el email del token JWT
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("¡El backend responde!");
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponseDTO> actualizarPerfil(
            @RequestBody @Valid PerfilUpdateDTO datos,
            Authentication authentication) {

        String email = authentication.getName();
        Usuario usuarioActualizado = usuarioService.actualizarPerfil(email, datos);

        return ResponseEntity.ok(new UsuarioResponseDTO(usuarioActualizado));
    }

    @PutMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @RequestBody @Valid CambioPasswordDTO datos,
            Authentication authentication) {

        usuarioService.cambiarPassword(authentication.getName(), datos);
        return ResponseEntity.noContent().build();
    }
}