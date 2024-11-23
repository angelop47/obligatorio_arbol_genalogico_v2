package com.example.obligatorio_arbol9.repository;

import com.example.obligatorio_arbol9.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    List<User> findAllByNombre(String nombre);
}