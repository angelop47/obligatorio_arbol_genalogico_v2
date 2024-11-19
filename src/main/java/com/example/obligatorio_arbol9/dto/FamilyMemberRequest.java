package com.example.obligatorio_arbol9.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberRequest {

    @NotNull
    private UserDTO familyMember;

    @NotNull
    private String relationship; // "antecesor" o "sucesor"
}