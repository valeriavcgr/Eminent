package com.example.Eminent.usuarios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Catálogo de roles del sistema. Los nombres posibles están controlados por {@link Usuario.Rol}. */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del rol. Único en el catálogo. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Usuario.Rol nombre;

    public Rol(Usuario.Rol nombre) {
        this.nombre = nombre;
    }
}
