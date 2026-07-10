package com.dev24.bookstore.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Customer signUp(String loginId, String rawPassword, String name, String nickname,
                            String email, String phone, String address, String interest, boolean newsletterYn) {
        if (customerRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        String passwordHash = passwordEncoder.encode(rawPassword);
        Customer customer = new Customer(loginId, passwordHash, name, nickname, email, phone, address, interest, newsletterYn);
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer authenticate(String loginId, String rawPassword) {
        Customer customer = customerRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(rawPassword, customer.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return customer;
    }
}
