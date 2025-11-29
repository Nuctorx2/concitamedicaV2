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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody RegistroUsuarioDTO datosRegistro) {
        Usuario nuevoUsuario = usuarioService.registrarUsuario(datosRegistro);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioActual(Authentication authentication) {
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