package com.concitamedica.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura IllegalStateException (usada para reglas de negocio)
     * y devuelve un 409 Conflict con el mensaje exacto.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(IllegalStateException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        errorResponse.put("error", "Conflict");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Captura errores de validación (como @NotBlank) si no se manejaron antes
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        // Si es otro error no controlado, devolvemos 400 o 500 según corresponda
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Captura errores de validación de DTO (@Valid).
     * Ejemplo: Contraseña corta, email inválido, documento largo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errorResponse = new HashMap<>();

        // Concatenamos todos los errores en un solo mensaje claro
        String mensaje = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(". "));

        errorResponse.put("message", mensaje);
        errorResponse.put("error", "Bad Request");

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Captura errores de base de datos (como duplicados o constraints que se pasaron del DTO)
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDbConstraint(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Error de integridad de datos. Posiblemente el email o documento ya existe, o el dato es muy largo.");
        errorResponse.put("error", "Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}