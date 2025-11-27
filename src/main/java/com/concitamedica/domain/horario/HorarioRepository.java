package com.concitamedica.domain.horario;

import com.concitamedica.domain.medico.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.concitamedica.domain.horario.DiaSemana;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;


@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findAllByMedico(Medico medico);

    List<Horario> findAllByMedicoId(Long medicoId);

    Optional<Horario> findByMedicoIdAndDiaSemana(Long medicoId, DiaSemana diaSemana);

    void deleteAllByMedicoId(Long medicoId);
}