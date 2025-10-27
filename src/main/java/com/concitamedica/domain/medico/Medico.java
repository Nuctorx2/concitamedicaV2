package com.concitamedica.domain.medico;

import com.concitamedica.domain.common.Auditable;
import com.concitamedica.domain.especialidad.Especialidad;
import com.concitamedica.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.concitamedica.domain.horario.Horario;
import java.util.List;


@Entity
@Table(name = "medicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medico extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Uno a Uno: Un perfil de médico corresponde a un único usuario.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false, unique = true)
    private Usuario usuario;

    // Relación Muchos a Uno: Muchos médicos pueden tener la misma especialidad.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", referencedColumnName = "id", nullable = false)
    private Especialidad especialidad;

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Indica que esta es la "parte principal" de la relación para JSON.
    private List<Horario> horarios;
}