package com.concitamedica.domain.medico;

import com.concitamedica.domain.especialidad.Especialidad;
import com.concitamedica.domain.especialidad.EspecialidadRepository;
import com.concitamedica.domain.medico.dto.ActualizacionMedicoDTO;
import com.concitamedica.domain.medico.dto.CreacionMedicoDTO;
import com.concitamedica.domain.medico.dto.MedicoResponseDTO;
import com.concitamedica.domain.rol.Rol;
import com.concitamedica.domain.rol.RolRepository;
import com.concitamedica.domain.usuario.Usuario;
import com.concitamedica.domain.usuario.UsuarioRepository;
import com.concitamedica.domain.horario.HorarioRepository;
import com.concitamedica.domain.horario.Horario;
import com.concitamedica.domain.horario.DiaSemana;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;
    private final HorarioRepository horarioRepository;

    /**
     * Crea un nuevo médico y le asigna un horario base automáticamente.
     */
    @Transactional
    public Medico crearMedico(CreacionMedicoDTO datos) {
        if (usuarioRepository.findByEmail(datos.email()).isPresent()) {
            throw new IllegalStateException("El correo electrónico ya está en uso.");
        }

        Rol rolMedico = rolRepository.findByNombre("ROLE_MEDICO")
                .orElseThrow(() -> new IllegalStateException("El rol de Médico no existe."));

        Especialidad especialidad = especialidadRepository.findById(datos.especialidadId())
                .orElseThrow(() -> new IllegalStateException("La especialidad con ID " + datos.especialidadId() + " no existe."));

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(datos.nombre())
                .apellido(datos.apellido())
                .documento(datos.documento())
                .telefono(datos.telefono())
                .direccion(datos.direccion())
                .email(datos.email())
                .password(passwordEncoder.encode(datos.password()))
                .fechaNacimiento(datos.fechaNacimiento())
                .genero(datos.genero())
                .rol(rolMedico)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        Medico nuevoMedico = Medico.builder()
                .usuario(usuarioGuardado)
                .especialidad(especialidad)
                .build();

        Medico medicoGuardado = medicoRepository.save(nuevoMedico);

        asignarHorarioBase(medicoGuardado);

        return medicoGuardado;
    }

    private void asignarHorarioBase(Medico medico) {
        List<DiaSemana> diasLaborables = List.of(
                DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES
        );

        for (DiaSemana dia : diasLaborables) {
            Horario horario = Horario.builder()
                    .medico(medico)
                    .diaSemana(dia)
                    .horaInicio(LocalTime.of(8, 0))
                    .horaFin(LocalTime.of(17, 0))
                    .build();
            horarioRepository.save(horario);
        }
    }

    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> obtenerTodosLosMedicos() {
        return medicoRepository.findAll()
                .stream()
                .map(this::convertirAMedicoResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MedicoResponseDTO> obtenerMedicoPorId(Long id) {
        return medicoRepository.findById(id)
                .map(this::convertirAMedicoResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> buscarPorEspecialidad(Long especialidadId) {
        return medicoRepository.findAllByEspecialidadId(especialidadId)
                .stream()
                .map(this::convertirAMedicoResponseDTO)
                .toList();
    }


    @Transactional
    public MedicoResponseDTO actualizarMedico(Long id, ActualizacionMedicoDTO datos) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + id));

        Usuario usuario = medico.getUsuario();

        // 1. Actualizar datos de Usuario (Todos los campos)
        usuario.setNombre(datos.nombre());
        usuario.setApellido(datos.apellido());
        usuario.setDocumento(datos.documento());
        usuario.setTelefono(datos.telefono());
        usuario.setDireccion(datos.direccion());
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setGenero(datos.genero());

        // Nota: No actualizamos email ni password aquí por seguridad

        // 2. Actualizar Especialidad si cambió
        if (!medico.getEspecialidad().getId().equals(datos.especialidadId())) {
            Especialidad nuevaEsp = especialidadRepository.findById(datos.especialidadId())
                    .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));
            medico.setEspecialidad(nuevaEsp);
        }

        usuarioRepository.save(usuario);
        Medico medicoActualizado = medicoRepository.save(medico);

        return convertirAMedicoResponseDTO(medicoActualizado);
    }

    @Transactional
    public void eliminarMedico(Long id) {
        if (medicoRepository.existsById(id)) {
            medicoRepository.deleteById(id);
        }
    }


    private MedicoResponseDTO convertirAMedicoResponseDTO(Medico medico) {
        Usuario u = medico.getUsuario();
        return new MedicoResponseDTO(
                medico.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getDocumento(),
                u.getTelefono(),
                u.getDireccion(),
                u.getFechaNacimiento(),
                u.getGenero(),
                medico.getEspecialidad().getNombre(),
                medico.getEspecialidad().getId()
        );
    }
}