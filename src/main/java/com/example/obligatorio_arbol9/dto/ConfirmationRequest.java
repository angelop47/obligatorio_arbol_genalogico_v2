package com.example.obligatorio_arbol9.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationRequest {
    @NotNull
    private Long confirmerId; // ID del usuario que confirma
}