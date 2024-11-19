package com.example.obligatorio_arbol9.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private LocalDate fechaNacimiento;

    private LocalDate fechaFallecimiento;

    private Integer grado;

    // Padres
    @ManyToMany
    @JoinTable(
            name = "user_parents",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id")
    )
    @Builder.Default
    private Set<User> padres = new HashSet<>();

    // Hijos
    @ManyToMany(mappedBy = "padres")
    @Builder.Default
    private Set<User> hijos = new HashSet<>();

    // Cónyuges
    @ManyToMany
    @JoinTable(
            name = "user_conyuges",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "conyuge_id")
    )
    @Builder.Default
    private Set<User> conyuges = new HashSet<>();
}