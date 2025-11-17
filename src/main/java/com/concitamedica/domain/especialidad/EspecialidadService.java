package com.concitamedica.domain.especialidad;

import com.concitamedica.domain.especialidad.dto.EspecialidadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    @Transactional(readOnly = true)
    public List<EspecialidadDTO> obtenerTodas() {
        return especialidadRepository.findAll()
                .stream()
                .map(EspecialidadDTO::fromEntity)
                .collect(Collectors.toList());
    }
}