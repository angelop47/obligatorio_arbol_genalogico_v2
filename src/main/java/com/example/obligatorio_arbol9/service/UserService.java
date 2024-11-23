package com.example.obligatorio_arbol9.service;

import com.example.obligatorio_arbol9.dto.ConfirmationRequest;
import com.example.obligatorio_arbol9.dto.UserDTO;
import com.example.obligatorio_arbol9.dto.UserSummaryDTO;
import com.example.obligatorio_arbol9.entity.ConfirmationStatus;
import com.example.obligatorio_arbol9.entity.User;
import com.example.obligatorio_arbol9.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Registro de usuario
    @Transactional
    public User registerUser(UserDTO userDTO) {
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado.");
        }

        User user = User.builder()
                .nombre(userDTO.getNombre())
                .email(userDTO.getEmail())
                .fechaNacimiento(userDTO.getFechaNacimiento())
                .fechaFallecimiento(userDTO.getFechaFallecimiento())
                .grado(0) // Grado 0 para el usuario de referencia
                .confirmationStatus(ConfirmationStatus.PENDING)
                .build();

        // Determinar si necesita confirmación
        if (isAdult(user)) {
            // Si es mayor de edad y no está fallecido, enviar invitación al propio usuario
            if (user.getFechaFallecimiento() == null) {

            } else {
                // Si está fallecido, necesita al menos 3 confirmaciones de familiares directos
            }
        } else {
            // Si es menor de edad, necesita confirmación de un progenitor o familiar de hasta segundo grado
        }

        return userRepository.save(user);
    }

    // Método para confirmar el registro de un usuario
    @Transactional
    public void confirmUser(Long userId, ConfirmationRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<User> optionalConfirmer = userRepository.findById(request.getConfirmerId());

        if (optionalUser.isPresent() && optionalConfirmer.isPresent()) {
            User user = optionalUser.get();
            User confirmer = optionalConfirmer.get();

            // Verificar si el confirmador es elegible
            if (isEligibleConfirmer(user, confirmer)) {
                // Agregar el confirmador al conjunto 'confirmedBy'
                user.getConfirmedBy().add(confirmer);
                userRepository.save(user);

                // Verificar si se cumplen las condiciones de confirmación
                if (shouldConfirm(user)) {
                    user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
                    userRepository.save(user);
                }
            } else {
                throw new RuntimeException("El usuario que confirma no es elegible.");
            }
        } else {
            throw new RuntimeException("Usuario o confirmador no encontrado.");
        }
    }


    // Método para determinar si una persona es mayor de edad
    private boolean isAdult(User user) {
        if (user.getFechaNacimiento() == null) return false;
        return Period.between(user.getFechaNacimiento(), LocalDate.now()).getYears() >= 18;
    }

    // Método para verificar la elegibilidad del confirmador
    private boolean isEligibleConfirmer(User user, User confirmer) {
        if (user.getFechaFallecimiento() != null) {
            // Si está fallecido, el confirmador debe ser un familiar directo
            Set<User> directFamily = new HashSet<>();
            directFamily.addAll(user.getPadres());
            directFamily.addAll(user.getHijos());
            directFamily.addAll(user.getConyuges());
            return directFamily.contains(confirmer);
        } else if (isAdult(user)) {
            // Si es mayor de edad y no está fallecido, el propio usuario puede confirmar
            return user.equals(confirmer);
        } else {
            // Si es menor de edad, el confirmador debe ser un progenitor o familiar de hasta segundo grado
            return isWithinDegree(user, confirmer, 2);
        }
    }

    // Método para verificar si el confirmador está dentro del grado permitido
    private boolean isWithinDegree(User user, User confirmer, int maxDegree) {
        if (user.equals(confirmer)) {
            return true;
        }
        Set<User> visited = new HashSet<>();
        Queue<User> queue = new LinkedList<>();
        queue.add(user);
        visited.add(user);
        int degree = 0;

        while (!queue.isEmpty() && degree < maxDegree) {
            int size = queue.size();
            degree++;
            for (int i = 0; i < size; i++) {
                User current = queue.poll();
                Set<User> relatives = new HashSet<>();
                relatives.addAll(current.getPadres());
                relatives.addAll(current.getHijos());
                relatives.addAll(current.getConyuges());

                for (User relative : relatives) {
                    if (relative.equals(confirmer)) {
                        return true;
                    }
                    if (!visited.contains(relative)) {
                        visited.add(relative);
                        queue.add(relative);
                    }
                }
            }
        }
        return false;
    }

    private boolean shouldConfirm(User user) {
        if (user.getFechaFallecimiento() != null) {
            // Requiere al menos 3 confirmaciones de familiares directos
            return user.getConfirmedBy().size() >= 3;
        } else {
            if (isAdult(user)) {
                // Si es mayor de edad, una confirmación es suficiente
                return user.getConfirmedBy().size() >= 1;
            } else {
                // Si es menor de edad, una confirmación de un progenitor o familiar hasta segundo grado
                return user.getConfirmedBy().size() >= 1;
            }
        }
    }

    // Completar datos del usuario
    public User updateUser(Long userId, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
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
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Integer newGrado;

            if (relationship.equalsIgnoreCase("antecesor")) {
                newGrado = user.getGrado() + 1;
            } else if (relationship.equalsIgnoreCase("sucesor")) {
                newGrado = user.getGrado() - 1;
            } else {
                throw new RuntimeException("Tipo de relación inválida");
            }

            // Verificar si el email del familiar ya existe
            if (userRepository.existsByEmail(familyMemberDTO.getEmail())) {
                throw new RuntimeException("El email del familiar ya está registrado.");
            }

            User familyMember = User.builder()
                    .nombre(familyMemberDTO.getNombre())
                    .email(familyMemberDTO.getEmail())
                    .fechaNacimiento(familyMemberDTO.getFechaNacimiento())
                    .fechaFallecimiento(familyMemberDTO.getFechaFallecimiento())
                    .grado(newGrado)
                    .confirmationStatus(ConfirmationStatus.PENDING)
                    .build();

            // Establecer relaciones bidireccionales
            if (relationship.equalsIgnoreCase("antecesor")) {
                familyMember.getHijos().add(user);
                user.getPadres().add(familyMember);
            } else if (relationship.equalsIgnoreCase("sucesor")) {
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
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Verificar si el email del cónyuge ya existe
            if (userRepository.existsByEmail(spouseDTO.getEmail())) {
                throw new RuntimeException("El email del cónyuge ya está registrado.");
            }

            User spouse = User.builder()
                    .nombre(spouseDTO.getNombre())
                    .email(spouseDTO.getEmail())
                    .fechaNacimiento(spouseDTO.getFechaNacimiento())
                    .fechaFallecimiento(spouseDTO.getFechaFallecimiento())
                    .grado(user.getGrado()) // Mismo grado que el usuario
                    .confirmationStatus(ConfirmationStatus.PENDING)
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
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Eliminar relaciones con padres
            Set<User> padres = new HashSet<>(user.getPadres());
            for (User parent : padres) {
                parent.getHijos().remove(user);
                user.getPadres().remove(parent);
            }

            // Eliminar relaciones con hijos
            Set<User> hijos = new HashSet<>(user.getHijos());
            for (User child : hijos) {
                child.getPadres().remove(user);
                user.getHijos().remove(child);
            }

            // Eliminar relaciones con cónyuges
            Set<User> conyuges = new HashSet<>(user.getConyuges());
            for (User spouse : conyuges) {
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
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    // Obtener tdoos los Usuarios
    public List<UserSummaryDTO> getAllUsersSummary() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserSummaryDTO.builder()
                        .id(user.getId())
                        .nombre(user.getNombre())
                        .fechaNacimiento(user.getFechaNacimiento())
                        .fechaFallecimiento(user.getFechaFallecimiento())
                        .email(user.getEmail())
                        .confirmationStatus(user.getConfirmationStatus())
                        .build())
                .collect(Collectors.toList());
    }
}