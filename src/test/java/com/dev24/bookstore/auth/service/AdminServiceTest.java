package com.dev24.bookstore.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AdminService adminService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(adminRepository, passwordEncoder);
    }

    @Test
    void authenticate_correctPassword_returnsAdmin() {
        String hash = passwordEncoder.encode("adminpassword123!");
        Admin admin = new Admin("admin01", hash, "관리자");
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin));

        Admin authenticated = adminService.authenticate("admin01", "adminpassword123!");

        assertThat(authenticated).isEqualTo(admin);
    }

    @Test
    void authenticate_wrongPassword_throwsInvalidCredentials() {
        String hash = passwordEncoder.encode("adminpassword123!");
        Admin admin = new Admin("admin01", hash, "관리자");
        given(adminRepository.findByLoginId("admin01")).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminService.authenticate("admin01", "wrongPwd"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void authenticate_unknownLoginId_throwsInvalidCredentials() {
        given(adminRepository.findByLoginId(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.authenticate("unknown", "adminpassword123!"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }
}
