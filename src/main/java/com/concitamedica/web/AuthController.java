package com.concitamedica.web;

import com.concitamedica.domain.usuario.Usuario;
import com.concitamedica.domain.usuario.UsuarioService;
import com.concitamedica.domain.usuario.dto.RegistroUsuarioDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.concitamedica.domain.usuario.dto.LoginRequestDTO;
import com.concitamedica.domain.usuario.dto.LoginResponseDTO;
import com.concitamedica.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.concitamedica.domain.rol.Roles;

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
    public ResponseEntity<Usuario> registrarPaciente(@Valid @RequestBody RegistroUsuarioDTO datosRegistro) {
        // 1. Delega la lógica de negocio al servicio.
        Usuario nuevoUsuario = usuarioService.registrarPaciente(datosRegistro);

        // 2. Construye y retorna una respuesta HTTP.
        // HttpStatus.CREATED es el código 201, la respuesta estándar para una creación exitosa.
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
}