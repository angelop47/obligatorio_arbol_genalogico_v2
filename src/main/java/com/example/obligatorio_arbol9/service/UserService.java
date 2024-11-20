package com.example.obligatorio_arbol9.service;

import com.example.obligatorio_arbol9.dto.UserDTO;
import com.example.obligatorio_arbol9.entity.User;
import com.example.obligatorio_arbol9.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Registro de usuario
    public User registerUser(UserDTO userDTO) {
        User user = User.builder()
                .nombre(userDTO.getNombre())
                .fechaNacimiento(userDTO.getFechaNacimiento())
                .fechaFallecimiento(userDTO.getFechaFallecimiento())
                .grado(0) // Grado 0 para el usuario de referencia
                .build();
        return userRepository.save(user);
    }

    // Completar datos del usuario
    public User updateUser(Long userId, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setNombre(userDTO.getNombre());
            user.setFechaNacimiento(userDTO.getFechaNacimiento());
            user.setFechaFallecimiento(userDTO.getFechaFallecimiento());
            return userRepository.save(user);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    // Añadir familiar
    @Transactional
    public void addFamilyMember(Long userId, UserDTO familyMemberDTO, String relationship) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            Integer newGrado;

            if(relationship.equalsIgnoreCase("antecesor")) {
                newGrado = user.getGrado() + 1;
            } else if(relationship.equalsIgnoreCase("sucesor")) {
                newGrado = user.getGrado() - 1;
            } else {
                throw new RuntimeException("Tipo de relación inválida");
            }

            User familyMember = User.builder()
                    .nombre(familyMemberDTO.getNombre())
                    .fechaNacimiento(familyMemberDTO.getFechaNacimiento())
                    .fechaFallecimiento(familyMemberDTO.getFechaFallecimiento())
                    .grado(newGrado)
                    .build();

            // Establecer relaciones bidireccionales
            if(relationship.equalsIgnoreCase("antecesor")) {
                familyMember.getHijos().add(user);
                user.getPadres().add(familyMember);
            } else if(relationship.equalsIgnoreCase("sucesor")) {
                familyMember.getPadres().add(user);
                user.getHijos().add(familyMember);
            }

            // Guardar los cambios
            userRepository.save(familyMember);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    // Añadir cónyuge
    @Transactional
    public void addSpouse(Long userId, UserDTO spouseDTO) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();
            User spouse = User.builder()
                    .nombre(spouseDTO.getNombre())
                    .fechaNacimiento(spouseDTO.getFechaNacimiento())
                    .fechaFallecimiento(spouseDTO.getFechaFallecimiento())
                    .grado(user.getGrado()) // Mismo grado que el usuario
                    .build();

            user.getConyuges().add(spouse);
            spouse.getConyuges().add(user);

            userRepository.save(spouse);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    // Método para borrar usuario
    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Eliminar relaciones con padres
            Set<User> padres = new HashSet<>(user.getPadres());
            for(User parent : padres) {
                parent.getHijos().remove(user);
                user.getPadres().remove(parent);
            }

            // Eliminar relaciones con hijos
            Set<User> hijos = new HashSet<>(user.getHijos());
            for(User child : hijos) {
                child.getPadres().remove(user);
                user.getHijos().remove(child);
            }

            // Eliminar relaciones con cónyuges
            Set<User> conyuges = new HashSet<>(user.getConyuges());
            for(User spouse : conyuges) {
                spouse.getConyuges().remove(user);
                user.getConyuges().remove(spouse);
            }

            // Finalmente, borrar el usuario
            userRepository.delete(user);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    // Obtener árbol genealógico
    public User getGenealogyTree(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }
}