package com.concitamedica.domain.paciente.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AgendarCitaDTO(
        @NotNull Long medicoId,
        @NotNull @Future LocalDateTime fechaHoraInicio
) {}