package com.dev24.bookstore.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Admin authenticate(String loginId, String rawPassword) {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(rawPassword, admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return admin;
    }
}
