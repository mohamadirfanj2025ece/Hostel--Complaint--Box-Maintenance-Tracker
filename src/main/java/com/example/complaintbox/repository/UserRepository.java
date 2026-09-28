package com.example.complaintbox.repository;

import com.example.complaintbox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(com.example.complaintbox.enums.Role role);

    boolean existsByRegisterNumber(String registerNumber);

    boolean existsByRegisterNumberAndIdNot(String registerNumber, Long id);

    Optional<User> findByRegisterNumber(String registerNumber);
}
