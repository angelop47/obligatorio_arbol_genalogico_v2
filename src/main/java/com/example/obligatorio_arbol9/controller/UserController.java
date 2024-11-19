package com.example.obligatorio_arbol9.controller;

import com.example.obligatorio_arbol9.dto.FamilyMemberRequest;
import com.example.obligatorio_arbol9.dto.UserDTO;
import com.example.obligatorio_arbol9.entity.User;
import com.example.obligatorio_arbol9.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. Alta de usuario
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserDTO userDTO) {
        User user = userService.registerUser(userDTO);
        return ResponseEntity.ok(user);
    }

    // 2. Completar datos del usuario
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @Valid @RequestBody UserDTO userDTO) {
        User updatedUser = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // 3. Registro de familiares
    @PostMapping("/{userId}/family")
    public ResponseEntity<User> addFamilyMember(
            @PathVariable Long userId,
            @Valid @RequestBody FamilyMemberRequest request) {
        userService.addFamilyMember(userId, request.getFamilyMember(), request.getRelationship());
        User tree = userService.getGenealogyTree(userId);
        return ResponseEntity.ok(tree);
    }

    // 4. Añadir cónyuge
    @PostMapping("/{userId}/spouse")
    public ResponseEntity<User> addSpouse(
            @PathVariable Long userId,
            @Valid @RequestBody UserDTO spouseDTO) {
        userService.addSpouse(userId, spouseDTO);
        User tree = userService.getGenealogyTree(userId);
        return ResponseEntity.ok(tree);
    }

    // 5. Obtener árbol genealógico
    @GetMapping("/{userId}/tree")
    public ResponseEntity<User> getGenealogyTree(@PathVariable Long userId) {
        User tree = userService.getGenealogyTree(userId);
        return ResponseEntity.ok(tree);
    }
}