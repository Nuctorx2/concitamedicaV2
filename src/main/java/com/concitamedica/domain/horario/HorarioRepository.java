package com.concitamedica.domain.horario;

import com.concitamedica.domain.medico.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.concitamedica.domain.horario.DiaSemana;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;

/**
 * Repositorio para la entidad Horario.
 */
@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

    /**
     * Busca todos los horarios definidos para un médico específico.
     * @param medico El médico cuyos horarios queremos obtener.
     * @return una lista de los horarios del médico.
     */
    List<Horario> findAllByMedico(Medico medico);

    /**
     * Busca todos los horarios asociados a un ID de médico específico.
     * Spring Data JPA genera la consulta a partir del nombre del método.
     * @param medicoId El ID del médico.
     * @return Lista de horarios encontrados.
     */
    List<Horario> findAllByMedicoId(Long medicoId);
    Optional<Horario> findByMedicoIdAndDiaSemana(Long medicoId, DiaSemana diaSemana);
}