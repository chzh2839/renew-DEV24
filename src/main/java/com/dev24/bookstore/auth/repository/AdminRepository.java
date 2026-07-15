package com.dev24.bookstore.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.AdminRole;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    List<Admin> findAllByAdminRole(AdminRole adminRole);
}
