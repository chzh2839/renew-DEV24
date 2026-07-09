package com.dev24.bookstore.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.auth.domain.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
