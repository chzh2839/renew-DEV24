package com.dev24.bookstore.auth.service;

import com.dev24.bookstore.auth.domain.Customer;

public interface CustomerService {

    Customer signUp(CustomerSignUpCommand command);

    Customer authenticate(String loginId, String rawPassword);
}
