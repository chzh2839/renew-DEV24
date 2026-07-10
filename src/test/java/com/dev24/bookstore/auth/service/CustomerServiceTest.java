package com.dev24.bookstore.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private CustomerService customerService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, passwordEncoder);
    }

    @Test
    void signUp_hashesPasswordWithBCrypt() {
        given(customerRepository.existsByLoginId("dev24")).willReturn(false);
        given(customerRepository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0));

        Customer customer = customerService.signUp(
                "dev24", "password123!", "홍길동", "gildong", "gildong@example.com", "010-1234-5678", "서울", "소설", true);

        assertThat(customer.getPasswordHash()).isNotEqualTo("password123!");
        assertThat(passwordEncoder.matches("password123!", customer.getPasswordHash())).isTrue();
    }

    @Test
    void signUp_duplicateLoginId_throwsBusinessException() {
        given(customerRepository.existsByLoginId("dev24")).willReturn(true);

        assertThatThrownBy(() -> customerService.signUp(
                "dev24", "password123!", "홍길동", "gildong", "gildong@example.com", "010-1234-5678", "서울", "소설", true))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID));
    }

    @Test
    void authenticate_correctPassword_returnsCustomer() {
        String hash = passwordEncoder.encode("password123!");
        Customer customer = new Customer("dev24", hash, "홍길동", "gildong", "gildong@example.com", "010-1234-5678", "서울", "소설", true);
        given(customerRepository.findByLoginId("dev24")).willReturn(Optional.of(customer));

        Customer authenticated = customerService.authenticate("dev24", "password123!");

        assertThat(authenticated).isEqualTo(customer);
    }

    @Test
    void authenticate_wrongPassword_throwsInvalidCredentials() {
        String hash = passwordEncoder.encode("password123!");
        Customer customer = new Customer("dev24", hash, "홍길동", "gildong", "gildong@example.com", "010-1234-5678", "서울", "소설", true);
        given(customerRepository.findByLoginId("dev24")).willReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerService.authenticate("dev24", "wrongPwd"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void authenticate_unknownLoginId_throwsInvalidCredentials() {
        given(customerRepository.findByLoginId(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.authenticate("unknown", "password123!"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }
}
