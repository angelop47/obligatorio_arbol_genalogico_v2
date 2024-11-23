package com.example.obligatorio_arbol9.dto;

import com.example.obligatorio_arbol9.entity.ConfirmationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {
    private Long id;
    private String nombre;
    private LocalDate fechaNacimiento;
    private LocalDate fechaFallecimiento;
    private String email;
    private ConfirmationStatus confirmationStatus;
}