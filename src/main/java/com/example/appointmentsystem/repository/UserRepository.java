package com.example.appointmentsystem.repository;

import com.example.appointmentsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    Page<User> findAll(Pageable pageable); // Do paginacji listy użytkowników
}
