package com.example.obligatorio_arbol9.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nombre;

    private LocalDate fechaNacimiento;

    private LocalDate fechaFallecimiento;

    private Integer grado;

    // Estado de confirmación: PENDING, CONFIRMED
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConfirmationStatus confirmationStatus = ConfirmationStatus.PENDING;

    // Familias que han confirmado
    @ManyToMany
    @JoinTable(
            name = "user_confirmations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "confirmed_by_id")
    )
    @JsonIgnore
    private Set<User> confirmedBy = new HashSet<>();

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

    // Getter personalizado para serializar solo los IDs de confirmedBy
    @JsonProperty("confirmedBy")
    public Set<Long> getConfirmedByIds() {
        if (confirmedBy == null) {
            return new HashSet<>();
        }
        return confirmedBy.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
}