package com.concitamedica.domain.medico;

import com.concitamedica.domain.especialidad.Especialidad;
import com.concitamedica.domain.especialidad.EspecialidadRepository;
import com.concitamedica.domain.medico.dto.CreacionMedicoDTO;
import com.concitamedica.domain.rol.Rol;
import com.concitamedica.domain.rol.RolRepository;
import com.concitamedica.domain.usuario.Usuario;
import com.concitamedica.domain.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.concitamedica.domain.medico.dto.MedicoResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import com.concitamedica.domain.medico.dto.ActualizacionMedicoDTO;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario con rol MÉDICO y su perfil de médico asociado.
     * Esta operación es transaccional: o se completan ambos guardados, o no se hace nada.
     * @param datos DTO con la información del nuevo médico.
     * @return La entidad Medico recién creada.
     */
    @Transactional
    public Medico crearMedico(CreacionMedicoDTO datos) {
        // 1. Validar que el email no exista
        if (usuarioRepository.findByEmail(datos.email()).isPresent()) {
            throw new IllegalStateException("El correo electrónico ya está en uso.");
        }

        // 2. Buscar los roles y especialidades necesarios
        Rol rolMedico = rolRepository.findByNombre("ROLE_MEDICO")
                .orElseThrow(() -> new IllegalStateException("El rol de Médico no existe."));

        Especialidad especialidad = especialidadRepository.findById(datos.especialidadId())
                .orElseThrow(() -> new IllegalStateException("La especialidad con ID " + datos.especialidadId() + " no existe."));

        // 3. Crear y guardar la entidad Usuario
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(datos.nombre())
                .email(datos.email())
                .password(passwordEncoder.encode(datos.password()))
                .fechaNacimiento(datos.fechaNacimiento())
                .genero(datos.genero())
                .rol(rolMedico)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario); // ¡Paso CRÍTICO!

        // 4. Crear y guardar la entidad Medico, vinculándola al usuario recién creado
        Medico nuevoMedico = Medico.builder()
                .usuario(usuarioGuardado)
                .especialidad(especialidad)
                .build();

        return medicoRepository.save(nuevoMedico);
    }

    /**
     * Obtiene una lista de todos los médicos registrados en el sistema.
     * @return Una lista de DTOs con la información pública de cada médico.
     */
    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> obtenerTodosLosMedicos() {
        return medicoRepository.findAll()
                .stream()
                .map(this::convertirAMedicoResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método de ayuda para convertir una entidad Medico a su DTO de respuesta.
     */
    private MedicoResponseDTO convertirAMedicoResponseDTO(Medico medico) {
        return new MedicoResponseDTO(
                medico.getId(),
                medico.getUsuario().getNombre(),
                medico.getUsuario().getEmail(),
                medico.getEspecialidad().getNombre()
        );
    }

    /**
     * Busca un médico por su ID.
     * @param id El ID del médico a buscar.
     * @return un Optional que contiene el DTO del médico si se encuentra.
     */
    @Transactional(readOnly = true)
    public Optional<MedicoResponseDTO> obtenerMedicoPorId(Long id) {
        return medicoRepository.findById(id)
                .map(this::convertirAMedicoResponseDTO);
    }

    /**
     * Actualiza la información de un médico existente.
     * @param id El ID del médico a actualizar.
     * @param datos DTO con los nuevos datos.
     * @return El DTO del médico con la información actualizada.
     */
    @Transactional
    public MedicoResponseDTO actualizarMedico(Long id, ActualizacionMedicoDTO datos) {
        // 1. Buscar el médico. Si no existe, lanza una excepción.
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + id)); // Podríamos usar una excepción personalizada

        // 2. Buscar la nueva especialidad. Si no existe, lanza una excepción.
        Especialidad nuevaEspecialidad = especialidadRepository.findById(datos.especialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada con ID: " + datos.especialidadId()));

        // 3. Actualizar los campos permitidos
        medico.getUsuario().setNombre(datos.nombre());
        medico.setEspecialidad(nuevaEspecialidad);

        // 4. Guardar los cambios. Como el método es @Transactional,
        // Hibernate detecta los cambios y los guarda automáticamente al final de la transacción.
        // No es estrictamente necesario llamar a save(), pero es una buena práctica ser explícito.
        Medico medicoActualizado = medicoRepository.save(medico);

        // 5. Devolver la vista actualizada del médico
        return convertirAMedicoResponseDTO(medicoActualizado);
    }

    /**
     * Elimina un médico por su ID.
     * Si el médico tiene datos asociados (citas, horarios),
     * es posible que se necesite una lógica más compleja.
     * @param id El ID del médico a eliminar.
     */
    @Transactional
    public void eliminarMedico(Long id) {
        // Primero verificamos si el médico existe para evitar procesar un ID inválido.
        if (!medicoRepository.existsById(id)) {
            // Podríamos lanzar una excepción si quisiéramos ser más estrictos
            // y devolver un 404, pero para DELETE, a menudo es aceptable no hacer nada.
            return;
        }
        medicoRepository.deleteById(id);
    }

    /**
     * Busca todos los médicos que coinciden con una especialidad dada.
     * @param especialidadId El ID de la especialidad.
     * @return Una lista de DTOs de médicos.
     */
    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> buscarPorEspecialidad(Long especialidadId) {
        return medicoRepository.findAllByEspecialidadId(especialidadId)
                .stream()
                .map(this::convertirAMedicoResponseDTO) // ¡Reutilizamos nuestro método de ayuda!
                .toList(); // .toList() es un atajo para .collect(Collectors.toList()) en Java 16+
    }

}