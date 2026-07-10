package com.dev24.bookstore.auth.service;

import com.dev24.bookstore.auth.domain.Customer;

public interface CustomerService {

    Customer signUp(String loginId, String rawPassword, String name, String nickname,
                     String email, String phone, String address, String interest, boolean newsletterYn);

    Customer authenticate(String loginId, String rawPassword);
}
